package br.com.gado.services;

import br.com.gado.dto.insumoDto.InsumoEstoqueCadastroDto;
import br.com.gado.dto.insumoDto.InsumoEstoquePutDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueRespostaDto;
import br.com.gado.dto.racaDto.RacaCadastroDto;
import br.com.gado.dto.racaDto.RacaPutDto;
import br.com.gado.dto.racaDto.RacaRespostaDto;
import br.com.gado.entities.EGrupoProduto;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.ERaca;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IGrupoProduto;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.IRaca;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SRacaTest {

    @InjectMocks
    private SRaca sRaca;

    @Mock private IRaca racaInterface;
    @Mock private IGrupoProduto grupoProdutoInterface;
    @Mock private IUnidadeMedida unidadeMedidaInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private IInsumo insumoInterface;
    @Mock private SInsumo insumoService;

    private EUsuario admin;
    private EGrupoProduto grupoAnimais;
    private EUnidadeMedida unidadeCabeca;

    @BeforeEach
    void setUp() {
        admin = new EUsuario();
        admin.setEmail("admin@fazenda.com");
        admin.setPerfil(EnPerfilUsuario.ADMINISTRADOR);

        grupoAnimais = new EGrupoProduto();
        grupoAnimais.setId(1L);
        grupoAnimais.setNome("Animais");

        unidadeCabeca = new EUnidadeMedida();
        unidadeCabeca.setId(1L);
        unidadeCabeca.setUnidade("CABECA");
    }

    private void stubUsuario(EUsuario usuario) {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(usuario));
    }

    @Nested
    class CriarTests {

        private RacaCadastroDto dtoValido() {
            RacaCadastroDto dto = new RacaCadastroDto();
            dto.setNome("Nelore");
            dto.setSigla("NE");
            return dto;
        }

        @Test
        void deveCriarRaca_EProdutoAssociado() {
            stubUsuario(admin);
            when(racaInterface.findFirstByNomeIgnoreCase("Nelore")).thenReturn(Optional.empty());
            when(racaInterface.findFirstBySiglaIgnoreCase("NE")).thenReturn(Optional.empty());
            when(grupoProdutoInterface.findFirstByNomeIgnoreCase("Animais")).thenReturn(Optional.of(grupoAnimais));
            when(unidadeMedidaInterface.findFirstByUnidadeIgnoreCase("CABECA")).thenReturn(Optional.of(unidadeCabeca));

            InsumoEstoqueRespostaDto produtoResposta = new InsumoEstoqueRespostaDto();
            produtoResposta.setId(500L);
            when(insumoService.criarInsumoEstoque(any(InsumoEstoqueCadastroDto.class), eq(admin.getEmail())))
                    .thenReturn(produtoResposta);

            EInsumo produto = new EInsumo();
            produto.setId(500L);
            produto.setNome("Gado Nelore");
            when(insumoInterface.findById(500L)).thenReturn(Optional.of(produto));
            when(racaInterface.save(any(ERaca.class))).thenAnswer(inv -> inv.getArgument(0));

            RacaRespostaDto resultado = sRaca.criar(dtoValido(), admin.getEmail());

            assertThat(resultado.getNome()).isEqualTo("Nelore");
            assertThat(resultado.getSigla()).isEqualTo("NE");
            assertThat(resultado.getProdutoId()).isEqualTo(500L);
        }

        @Test
        void deveLancarExcecao_QuandoNomeJaExiste() {
            stubUsuario(admin);
            when(racaInterface.findFirstByNomeIgnoreCase("Nelore")).thenReturn(Optional.of(new ERaca()));

            assertThatThrownBy(() -> sRaca.criar(dtoValido(), admin.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Já existe uma raça com esse nome");
        }

        @Test
        void deveLancarExcecao_QuandoSiglaJaExiste() {
            stubUsuario(admin);
            when(racaInterface.findFirstByNomeIgnoreCase("Nelore")).thenReturn(Optional.empty());
            when(racaInterface.findFirstBySiglaIgnoreCase("NE")).thenReturn(Optional.of(new ERaca()));

            assertThatThrownBy(() -> sRaca.criar(dtoValido(), admin.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sigla");
        }

        @Test
        void deveLancarIllegalStateException_QuandoGrupoAnimaisNaoExiste() {
            stubUsuario(admin);
            when(racaInterface.findFirstByNomeIgnoreCase("Nelore")).thenReturn(Optional.empty());
            when(racaInterface.findFirstBySiglaIgnoreCase("NE")).thenReturn(Optional.empty());
            when(grupoProdutoInterface.findFirstByNomeIgnoreCase("Animais")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sRaca.criar(dtoValido(), admin.getEmail()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioSemPermissao() {
            EUsuario cuidador = new EUsuario();
            cuidador.setEmail("cuidador@fazenda.com");
            cuidador.setPerfil(EnPerfilUsuario.CUIDADOR);
            stubUsuario(cuidador);

            assertThatThrownBy(() -> sRaca.criar(dtoValido(), cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(racaInterface);
        }
    }

    @Nested
    class AtualizarTests {

        private ERaca racaExistente() {
            EInsumo produto = new EInsumo();
            produto.setId(500L);
            ERaca raca = new ERaca();
            raca.setId(1L);
            raca.setNome("Nelore");
            raca.setSigla("NE");
            raca.setProduto(produto);
            return raca;
        }

        @Test
        void deveAtualizarNome_EProdutoAssociado() {
            stubUsuario(admin);
            ERaca raca = racaExistente();
            when(racaInterface.findById(1L)).thenReturn(Optional.of(raca));
            when(racaInterface.findFirstByNomeIgnoreCase("Angus")).thenReturn(Optional.empty());
            when(racaInterface.save(any(ERaca.class))).thenAnswer(inv -> inv.getArgument(0));

            RacaPutDto dto = new RacaPutDto();
            dto.setNome("Angus");

            RacaRespostaDto resultado = sRaca.atualizar(1L, dto, admin.getEmail());

            assertThat(resultado.getNome()).isEqualTo("Angus");
            verify(insumoService).atualizarDadosEstoque(eq(500L), any(InsumoEstoquePutDto.class), eq(admin.getEmail()));
        }

        @Test
        void deveLancarExcecao_QuandoNovoNomeConflitaComOutraRaca() {
            stubUsuario(admin);
            ERaca raca = racaExistente();
            ERaca outraRaca = new ERaca();
            outraRaca.setId(2L);
            when(racaInterface.findById(1L)).thenReturn(Optional.of(raca));
            when(racaInterface.findFirstByNomeIgnoreCase("Angus")).thenReturn(Optional.of(outraRaca));

            RacaPutDto dto = new RacaPutDto();
            dto.setNome("Angus");

            assertThatThrownBy(() -> sRaca.atualizar(1L, dto, admin.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoRacaNaoExiste() {
            stubUsuario(admin);
            when(racaInterface.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sRaca.atualizar(99L, new RacaPutDto(), admin.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class InativarReativarTests {

        @Test
        void deveInativarRaca_EProdutoAssociado() {
            stubUsuario(admin);
            EInsumo produto = new EInsumo();
            produto.setId(500L);
            ERaca raca = new ERaca();
            raca.setId(1L);
            raca.setProduto(produto);
            when(racaInterface.findById(1L)).thenReturn(Optional.of(raca));

            String resultado = sRaca.inativar(1L, admin.getEmail());

            assertThat(resultado).contains("inativada com sucesso");
            assertThat(raca.getStatus()).isEqualTo(EnStatus.I);
            verify(insumoService).inativarInsumo(500L, admin.getEmail());
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoRacaNaoExisteAoInativar() {
            stubUsuario(admin);
            when(racaInterface.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sRaca.inativar(99L, admin.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ListarTests {

        @Test
        void deveLancarExcecao_QuandoStatusInvalido() {
            assertThatThrownBy(() -> sRaca.listar(null, "X"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void deveListarTodas_SemFiltro() {
            when(racaInterface.findAllByOrderByNomeAsc()).thenReturn(java.util.List.of(new ERaca()));
            assertThat(sRaca.listar(null, null)).hasSize(1);
        }
    }

    @Nested
    class BuscarPorIdTests {

        @Test
        void deveLancarEntityNotFoundException_QuandoNaoEncontrada() {
            when(racaInterface.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sRaca.buscarPorId(99L)).isInstanceOf(EntityNotFoundException.class);
        }
    }
}
