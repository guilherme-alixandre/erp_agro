package br.com.gado.services;

import br.com.gado.dto.parcerioDto.ParceiroCadastroDto;
import br.com.gado.dto.parcerioDto.ParceiroDto;
import br.com.gado.dto.parcerioDto.ParceiroPutDto;
import br.com.gado.entities.EParceiro;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoParceiro;
import br.com.gado.repositories.IParceiro;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SParceiroTest {

    @InjectMocks
    private SParceiro sParceiro;

    @Mock private IParceiro parceiroInterface;
    @Mock private IUsuario usuarioInterface;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    private EUsuario gerente;
    private static final String CPF_VALIDO = "12345678909";

    @BeforeEach
    void setUp() {
        gerente = new EUsuario();
        gerente.setEmail("gerente@fazenda.com");
        gerente.setPerfil(EnPerfilUsuario.GERENTE);
    }

    private void stubUsuario(EUsuario usuario) {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(usuario));
    }

    @Nested
    class BuscaPorCpfCnpjTests {

        @Test
        void deveRetornarParceiro_QuandoEncontrado() {
            stubUsuario(gerente);
            EParceiro parceiro = new EParceiro();
            parceiro.setCpfCnpj(CPF_VALIDO);
            parceiro.setNome("Fornecedor Teste");
            when(parceiroInterface.findByCpfCnpj(CPF_VALIDO)).thenReturn(Optional.of(parceiro));

            ParceiroDto resultado = sParceiro.buscaPorCPF_CNPJ(CPF_VALIDO, gerente.getEmail());

            assertThat(resultado.getNome()).isEqualTo("Fornecedor Teste");
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoNaoEncontrado() {
            stubUsuario(gerente);
            when(parceiroInterface.findByCpfCnpj("99999999999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sParceiro.buscaPorCPF_CNPJ("99999999999", gerente.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ListarTodosTests {

        @Test
        void deveListarTodosOsParceirosAtivos() {
            stubUsuario(gerente);
            EParceiro parceiro = new EParceiro();
            parceiro.setCpfCnpj(CPF_VALIDO);
            parceiro.setTipo(EnTipoParceiro.FORNECEDOR);
            when(parceiroInterface.findByStatus(EnStatus.A)).thenReturn(List.of(parceiro));

            List<ParceiroDto> resultado = sParceiro.listarTodos(null, gerente.getEmail());

            assertThat(resultado).hasSize(1);
        }

        @Test
        void deveFiltrarPorTipo() {
            stubUsuario(gerente);
            EParceiro fornecedor = new EParceiro();
            fornecedor.setCpfCnpj(CPF_VALIDO);
            fornecedor.setTipo(EnTipoParceiro.FORNECEDOR);
            EParceiro comprador = new EParceiro();
            comprador.setCpfCnpj("98765432100");
            comprador.setTipo(EnTipoParceiro.COMPRADOR);
            when(parceiroInterface.findByStatus(EnStatus.A)).thenReturn(List.of(fornecedor, comprador));

            List<ParceiroDto> resultado = sParceiro.listarTodos("FORNECEDOR", gerente.getEmail());

            assertThat(resultado).hasSize(1);
        }
    }

    @Nested
    class CadastraTests {

        private ParceiroCadastroDto dtoValido() {
            ParceiroCadastroDto dto = new ParceiroCadastroDto();
            dto.setNome("Fornecedor Teste");
            dto.setCPF_CNPJ(CPF_VALIDO);
            dto.setTipo(EnTipoParceiro.FORNECEDOR);
            return dto;
        }

        @Test
        void deveCadastrarParceiro_QuandoDocumentoValido() {
            stubUsuario(gerente);
            when(parceiroInterface.existsByCpfCnpj(CPF_VALIDO)).thenReturn(false);
            when(parceiroInterface.save(any(EParceiro.class))).thenAnswer(inv -> inv.getArgument(0));

            ParceiroDto resultado = sParceiro.cadastra(dtoValido(), gerente.getEmail());

            assertThat(resultado.getNome()).isEqualTo("Fornecedor Teste");
        }

        @Test
        void deveLancarExcecao_QuandoDocumentoInvalido() {
            stubUsuario(gerente);
            ParceiroCadastroDto dto = dtoValido();
            dto.setCPF_CNPJ("00000000000");

            assertThatThrownBy(() -> sParceiro.cadastra(dto, gerente.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CPF/CNPJ inválido");
        }

        @Test
        void deveLancarExcecao_QuandoParceiroJaExiste() {
            stubUsuario(gerente);
            when(parceiroInterface.existsByCpfCnpj(CPF_VALIDO)).thenReturn(true);

            assertThatThrownBy(() -> sParceiro.cadastra(dtoValido(), gerente.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Parceiro já existe");
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioSemPermissaoGerencial() {
            EUsuario financeiro = new EUsuario();
            financeiro.setEmail("financeiro@fazenda.com");
            financeiro.setPerfil(EnPerfilUsuario.FINANCEIRO);
            stubUsuario(financeiro);

            assertThatThrownBy(() -> sParceiro.cadastra(dtoValido(), financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Apenas Administrador ou Gerente");
        }
    }

    @Nested
    class DeletaTests {

        @Test
        void deveDeletarParceiro_QuandoExiste() {
            stubUsuario(gerente);
            when(parceiroInterface.existsByCpfCnpj(CPF_VALIDO)).thenReturn(true);

            String resultado = sParceiro.deleta(CPF_VALIDO, gerente.getEmail());

            assertThat(resultado).isEqualTo("Parceiro deletado com sucesso");
            verify(parceiroInterface).deleteByCpfCnpj(CPF_VALIDO);
        }

        @Test
        void deveRetornarMensagem_QuandoNaoExiste() {
            stubUsuario(gerente);
            when(parceiroInterface.existsByCpfCnpj("99999999999")).thenReturn(false);

            String resultado = sParceiro.deleta("99999999999", gerente.getEmail());

            assertThat(resultado).contains("não existe no banco de dados");
        }
    }

    @Nested
    class AlteraTests {

        @Test
        void deveAtualizarCamposInformados() {
            stubUsuario(gerente);
            EParceiro parceiro = new EParceiro();
            parceiro.setCpfCnpj(CPF_VALIDO);
            parceiro.setNome("Nome antigo");
            when(parceiroInterface.findByCpfCnpj(CPF_VALIDO)).thenReturn(Optional.of(parceiro));
            when(parceiroInterface.save(any(EParceiro.class))).thenAnswer(inv -> inv.getArgument(0));

            ParceiroPutDto dto = new ParceiroPutDto();
            dto.setNome("Nome novo");

            ParceiroDto resultado = sParceiro.altera(CPF_VALIDO, dto, gerente.getEmail());

            assertThat(resultado.getNome()).isEqualTo("Nome novo");
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoParceiroNaoExiste() {
            stubUsuario(gerente);
            when(parceiroInterface.findByCpfCnpj("99999999999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sParceiro.altera("99999999999", new ParceiroPutDto(), gerente.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
