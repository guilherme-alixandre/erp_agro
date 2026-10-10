package br.com.gado.services;

import br.com.gado.dto.insumoDto.EntradaEstoqueDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueCadastroDto;
import br.com.gado.dto.insumoDto.InsumoEstoquePutDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueRespostaDto;
import br.com.gado.entities.EGrupoProduto;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.EMovimentacaoEstoque;
import br.com.gado.entities.EParceiro;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoInsumo;
import br.com.gado.enums.EnTipoMovimentacaoEstoque;
import br.com.gado.repositories.IGrupoProduto;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.IMovimentacaoEstoque;
import br.com.gado.repositories.IParceiro;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SInsumoTest {

    @InjectMocks
    private SInsumo sInsumo;

    @Mock private IInsumo insumoInterface;
    @Mock private IGrupoProduto grupoProdutoInterface;
    @Mock private IParceiro parceiroInterface;
    @Mock private IUnidadeMedida unidadeMedidaInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private IMovimentacaoEstoque movimentacaoEstoqueInterface;

    private EUsuario admin;
    private EUsuario cuidador;
    private EUnidadeMedida primaria;
    private EGrupoProduto grupo;

    @BeforeEach
    void setUp() {
        admin = new EUsuario();
        admin.setEmail("admin@fazenda.com");
        admin.setPerfil(EnPerfilUsuario.ADMINISTRADOR);

        cuidador = new EUsuario();
        cuidador.setEmail("cuidador@fazenda.com");
        cuidador.setPerfil(EnPerfilUsuario.CUIDADOR);

        primaria = new EUnidadeMedida();
        primaria.setId(1L);
        primaria.setUnidade("SACA");

        grupo = new EGrupoProduto();
        grupo.setId(5L);
        grupo.setNome("Rações");
        grupo.setCodigoPrefixo("01");
    }

    private void stubUsuario(EUsuario usuario) {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(usuario));
    }

    @Nested
    class ValidaGestaoEstoqueTests {

        @Test
        void devePermitir_QuandoAdministrador() {
            stubUsuario(admin);
            sInsumo.validaGestaoEstoque(admin.getEmail());
        }

        @Test
        void deveNegar_QuandoCuidadorComum() {
            stubUsuario(cuidador);
            assertThatThrownBy(() -> sInsumo.validaGestaoEstoque(cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Administradores, Gerentes ou Cuidadores Chefe");
        }
    }

    @Nested
    class ListarEstoqueTests {

        @Test
        void deveListarTodos_SemFiltro() {
            when(insumoInterface.findAllByOrderByNomeAsc()).thenReturn(List.of(new EInsumo()));
            assertThat(sInsumo.listarEstoque(null, null)).hasSize(1);
        }

        @Test
        void deveFiltrarPorBuscaEStatus() {
            when(insumoInterface.findByStatusAndNomeContainingIgnoreCaseOrderByNomeAsc(EnStatus.A, "raç"))
                    .thenReturn(List.of(new EInsumo()));
            assertThat(sInsumo.listarEstoque("raç", "A")).hasSize(1);
        }

        @Test
        void deveLancarExcecao_QuandoStatusInvalido() {
            assertThatThrownBy(() -> sInsumo.listarEstoque(null, "X"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class CriarInsumoEstoqueTests {

        private InsumoEstoqueCadastroDto dtoValido() {
            InsumoEstoqueCadastroDto dto = new InsumoEstoqueCadastroDto();
            dto.setNome("Ração Bovina");
            dto.setTipo(EnTipoInsumo.RACAO);
            dto.setGrupoProdutoId(grupo.getId());
            dto.setUnidadeMedidaPrimariaId(primaria.getId());
            return dto;
        }

        @Test
        void deveCriarComPrimeiroCodigoDoGrupo_QuandoGrupoSemProdutosAnteriores() {
            stubUsuario(admin);
            when(unidadeMedidaInterface.findById(primaria.getId())).thenReturn(Optional.of(primaria));
            when(grupoProdutoInterface.findById(grupo.getId())).thenReturn(Optional.of(grupo));
            when(insumoInterface.findFirstByCodigoProdutoBetweenOrderByCodigoProdutoDesc("01000000", "01999999"))
                    .thenReturn(Optional.empty());
            when(insumoInterface.saveAndFlush(any(EInsumo.class))).thenAnswer(inv -> inv.getArgument(0));

            InsumoEstoqueRespostaDto resultado = sInsumo.criarInsumoEstoque(dtoValido(), admin.getEmail());

            assertThat(resultado.getCodigoProduto()).isEqualTo("01000001");
            assertThat(resultado.getSaldoAtual()).isEqualTo(0.0);
        }

        @Test
        void deveIncrementarUltimoCodigo_QuandoJaExistemProdutosNoGrupo() {
            stubUsuario(admin);
            when(unidadeMedidaInterface.findById(primaria.getId())).thenReturn(Optional.of(primaria));
            when(grupoProdutoInterface.findById(grupo.getId())).thenReturn(Optional.of(grupo));
            EInsumo ultimo = new EInsumo();
            ultimo.setCodigoProduto("01000005");
            when(insumoInterface.findFirstByCodigoProdutoBetweenOrderByCodigoProdutoDesc("01000000", "01999999"))
                    .thenReturn(Optional.of(ultimo));
            when(insumoInterface.saveAndFlush(any(EInsumo.class))).thenAnswer(inv -> inv.getArgument(0));

            InsumoEstoqueRespostaDto resultado = sInsumo.criarInsumoEstoque(dtoValido(), admin.getEmail());

            assertThat(resultado.getCodigoProduto()).isEqualTo("01000006");
        }

        @Test
        void deveLancarExcecao_QuandoUnidadeSecundariaSemFatorConversao() {
            stubUsuario(admin);
            EUnidadeMedida secundaria = new EUnidadeMedida();
            secundaria.setId(2L);
            when(unidadeMedidaInterface.findById(primaria.getId())).thenReturn(Optional.of(primaria));
            when(unidadeMedidaInterface.findById(2L)).thenReturn(Optional.of(secundaria));

            InsumoEstoqueCadastroDto dto = dtoValido();
            dto.setUnidadeMedidaSecundariaId(2L);

            assertThatThrownBy(() -> sInsumo.criarInsumoEstoque(dto, admin.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("fator de conversão");
        }

        @Test
        void deveLancarExcecao_QuandoGrupoNaoEncontrado() {
            stubUsuario(admin);
            when(unidadeMedidaInterface.findById(primaria.getId())).thenReturn(Optional.of(primaria));
            when(grupoProdutoInterface.findById(grupo.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sInsumo.criarInsumoEstoque(dtoValido(), admin.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Grupo de produto não encontrado");
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioSemPermissao() {
            stubUsuario(cuidador);
            assertThatThrownBy(() -> sInsumo.criarInsumoEstoque(dtoValido(), cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(unidadeMedidaInterface, grupoProdutoInterface);
        }
    }

    @Nested
    class AtualizarDadosEstoqueTests {

        private EInsumo insumoExistente() {
            EInsumo insumo = new EInsumo();
            insumo.setId(100L);
            insumo.setNome("Ração Bovina");
            insumo.setUnidadeMedidaPrimaria(primaria);
            insumo.setStatus(EnStatus.A);
            return insumo;
        }

        @Test
        void deveAtualizarNome() {
            stubUsuario(admin);
            EInsumo insumo = insumoExistente();
            when(insumoInterface.findByIdAndStatus(100L, EnStatus.A)).thenReturn(Optional.of(insumo));
            when(insumoInterface.save(any(EInsumo.class))).thenAnswer(inv -> inv.getArgument(0));

            InsumoEstoquePutDto dto = new InsumoEstoquePutDto();
            dto.setNome("Ração Suína");

            InsumoEstoqueRespostaDto resultado = sInsumo.atualizarDadosEstoque(100L, dto, admin.getEmail());

            assertThat(resultado.getNome()).isEqualTo("Ração Suína");
        }

        @Test
        void deveLancarExcecao_QuandoNomeVazio() {
            stubUsuario(admin);
            when(insumoInterface.findByIdAndStatus(100L, EnStatus.A)).thenReturn(Optional.of(insumoExistente()));

            InsumoEstoquePutDto dto = new InsumoEstoquePutDto();
            dto.setNome("   ");

            assertThatThrownBy(() -> sInsumo.atualizarDadosEstoque(100L, dto, admin.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void deveArredondarPrecos_ParaDuasCasas() {
            stubUsuario(admin);
            EInsumo insumo = insumoExistente();
            when(insumoInterface.findByIdAndStatus(100L, EnStatus.A)).thenReturn(Optional.of(insumo));
            when(insumoInterface.save(any(EInsumo.class))).thenAnswer(inv -> inv.getArgument(0));

            InsumoEstoquePutDto dto = new InsumoEstoquePutDto();
            dto.setPrecoCompraMedio(10.005);

            InsumoEstoqueRespostaDto resultado = sInsumo.atualizarDadosEstoque(100L, dto, admin.getEmail());

            assertThat(resultado.getPrecoCompraMedio()).isEqualTo(10.01);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoInsumoNaoExiste() {
            stubUsuario(admin);
            when(insumoInterface.findByIdAndStatus(999L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sInsumo.atualizarDadosEstoque(999L, new InsumoEstoquePutDto(), admin.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class RegistrarEntradaEstoqueTests {

        private EntradaEstoqueDto dto(double quantidade, double precoUnitario) {
            EntradaEstoqueDto dto = new EntradaEstoqueDto();
            dto.setQuantidade(quantidade);
            dto.setPrecoUnitario(precoUnitario);
            return dto;
        }

        @Test
        void devePrecoMedioIgualAoDaEntrada_QuandoPrimeiraEntrada() {
            stubUsuario(admin);
            EInsumo insumo = new EInsumo();
            insumo.setId(100L);
            insumo.setSaldoAtual(0.0);
            insumo.setUnidadeMedidaPrimaria(primaria);
            when(insumoInterface.findByIdAndStatus(100L, EnStatus.A)).thenReturn(Optional.of(insumo));
            when(insumoInterface.save(any(EInsumo.class))).thenAnswer(inv -> inv.getArgument(0));

            InsumoEstoqueRespostaDto resultado = sInsumo.registrarEntradaEstoque(100L, dto(10.0, 7.0), admin.getEmail());

            assertThat(resultado.getSaldoAtual()).isEqualTo(10.0);
            assertThat(resultado.getPrecoCompraMedio()).isEqualTo(7.0);
            verify(movimentacaoEstoqueInterface).save(any(EMovimentacaoEstoque.class));
        }

        @Test
        void deveCalcularMediaPonderada_QuandoJaHavianoSaldo() {
            stubUsuario(admin);
            EInsumo insumo = new EInsumo();
            insumo.setId(100L);
            insumo.setSaldoAtual(10.0);
            insumo.setPrecoCompraMedio(5.0);
            insumo.setUnidadeMedidaPrimaria(primaria);
            when(insumoInterface.findByIdAndStatus(100L, EnStatus.A)).thenReturn(Optional.of(insumo));
            when(insumoInterface.save(any(EInsumo.class))).thenAnswer(inv -> inv.getArgument(0));

            InsumoEstoqueRespostaDto resultado = sInsumo.registrarEntradaEstoque(100L, dto(10.0, 7.0), admin.getEmail());

            // (10*5 + 10*7) / 20 = 6.0
            assertThat(resultado.getSaldoAtual()).isEqualTo(20.0);
            assertThat(resultado.getPrecoCompraMedio()).isEqualTo(6.0);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoInsumoNaoExiste() {
            stubUsuario(admin);
            when(insumoInterface.findByIdAndStatus(999L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sInsumo.registrarEntradaEstoque(999L, dto(1.0, 1.0), admin.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class BaixarEstoqueTests {

        @Test
        void deveBaixarEstoque_QuandoSaldoSuficiente() {
            EInsumo insumo = new EInsumo();
            insumo.setId(100L);
            insumo.setNome("Cabeça Nelore");
            insumo.setSaldoAtual(5.0);
            when(insumoInterface.findByIdAndStatus(100L, EnStatus.A)).thenReturn(Optional.of(insumo));
            when(insumoInterface.save(any(EInsumo.class))).thenAnswer(inv -> inv.getArgument(0));

            sInsumo.baixarEstoque(100L, 1.0, null, LocalDate.now());

            assertThat(insumo.getSaldoAtual()).isEqualTo(4.0);
            verify(movimentacaoEstoqueInterface).save(any(EMovimentacaoEstoque.class));
        }

        @Test
        void deveLancarExcecao_QuandoEstoqueInsuficiente() {
            EInsumo insumo = new EInsumo();
            insumo.setId(100L);
            insumo.setNome("Cabeça Nelore");
            insumo.setSaldoAtual(0.0);
            when(insumoInterface.findByIdAndStatus(100L, EnStatus.A)).thenReturn(Optional.of(insumo));

            assertThatThrownBy(() -> sInsumo.baixarEstoque(100L, 1.0, null, LocalDate.now()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Estoque insuficiente");
        }
    }

    @Nested
    class GestaoDeStatusTests {

        @Test
        void deveInativarInsumo() {
            stubUsuario(admin);
            EInsumo insumo = new EInsumo();
            insumo.setId(100L);
            when(insumoInterface.findById(100L)).thenReturn(Optional.of(insumo));

            String resultado = sInsumo.inativarInsumo(100L, admin.getEmail());

            assertThat(resultado).contains("inativado com sucesso");
            assertThat(insumo.getStatus()).isEqualTo(EnStatus.I);
        }

        @Test
        void deveReativarInsumo() {
            stubUsuario(admin);
            EInsumo insumo = new EInsumo();
            insumo.setId(100L);
            insumo.setStatus(EnStatus.I);
            when(insumoInterface.findById(100L)).thenReturn(Optional.of(insumo));

            String resultado = sInsumo.reativarInsumo(100L, admin.getEmail());

            assertThat(resultado).contains("reativado com sucesso");
            assertThat(insumo.getStatus()).isEqualTo(EnStatus.A);
        }

        @Test
        void deveExcluirDefinitivamente_QuandoInativo() {
            stubUsuario(admin);
            EInsumo insumo = new EInsumo();
            insumo.setId(100L);
            insumo.setStatus(EnStatus.I);
            when(insumoInterface.findById(100L)).thenReturn(Optional.of(insumo));

            String resultado = sInsumo.excluirDefinitivamenteInsumo(100L, admin.getEmail());

            assertThat(resultado).contains("excluído definitivamente");
            verify(insumoInterface).delete(insumo);
        }

        @Test
        void deveLancarExcecao_QuandoExcluirInsumoAindaAtivo() {
            stubUsuario(admin);
            EInsumo insumo = new EInsumo();
            insumo.setId(100L);
            insumo.setStatus(EnStatus.A);
            when(insumoInterface.findById(100L)).thenReturn(Optional.of(insumo));

            assertThatThrownBy(() -> sInsumo.excluirDefinitivamenteInsumo(100L, admin.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Apenas produtos inativos");
            verify(insumoInterface, never()).delete(any());
        }
    }
}
