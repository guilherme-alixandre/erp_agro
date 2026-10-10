package br.com.gado.services;

import br.com.gado.dto.folhaPagamentoDto.EstornoPagamentoDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioCadastroDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioPutDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioRespostaDto;
import br.com.gado.dto.folhaPagamentoDto.PagamentoFuncionarioCadastroDto;
import br.com.gado.dto.folhaPagamentoDto.PagamentoFuncionarioRespostaDto;
import br.com.gado.entities.EFuncionario;
import br.com.gado.entities.EPagamentoFuncionario;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnStatusDespesa;
import br.com.gado.repositories.IFuncionario;
import br.com.gado.repositories.IPagamentoFuncionario;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SFolhaPagamentoTest {

    @InjectMocks
    private SFolhaPagamento sFolhaPagamento;

    @Mock private IFuncionario funcionarioInterface;
    @Mock private IPagamentoFuncionario pagamentoInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private SLancamentoFinanceiro lancamentoFinanceiroService;

    private EUsuario gerente;

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

    private EFuncionario funcionario() {
        EFuncionario funcionario = new EFuncionario();
        funcionario.setId(1L);
        funcionario.setNomeCompleto("João Cuidador");
        funcionario.setSalarioBase(new BigDecimal("3000.00"));
        funcionario.setPercentualInss(new BigDecimal("9.00"));
        funcionario.setPercentualFgts(new BigDecimal("8.00"));
        funcionario.setValorValeTransporte(new BigDecimal("100.00"));
        funcionario.setValorValeAlimentacao(new BigDecimal("200.00"));
        funcionario.setValorPlanoSaude(new BigDecimal("50.00"));
        funcionario.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);
        return funcionario;
    }

    @Nested
    class CadastrarFuncionarioTests {

        private FuncionarioCadastroDto dtoValido() {
            FuncionarioCadastroDto dto = new FuncionarioCadastroDto();
            dto.setNomeCompleto("João Cuidador");
            dto.setCpf("12345678909");
            dto.setCargo("cuidador");
            dto.setDataAdmissao(LocalDate.of(2025, 1, 1));
            dto.setSalarioBase(new BigDecimal("3000.00"));
            dto.setPercentualInss(new BigDecimal("9.00"));
            dto.setPercentualFgts(new BigDecimal("8.00"));
            return dto;
        }

        @Test
        void deveCadastrarFuncionario_ComNaturezaFinanceiraSempreCusto() {
            stubUsuario(gerente);
            when(funcionarioInterface.findByCpf("12345678909")).thenReturn(Optional.empty());
            when(funcionarioInterface.save(any(EFuncionario.class))).thenAnswer(inv -> {
                EFuncionario f = inv.getArgument(0);
                f.setId(1L);
                return f;
            });

            FuncionarioRespostaDto resultado = sFolhaPagamento.cadastrarFuncionario(dtoValido(), gerente.getEmail());

            assertThat(resultado.getCargo()).isEqualTo("CUIDADOR");
            assertThat(resultado.getNaturezaFinanceira()).isEqualTo(EnNaturezaFinanceira.CUSTO);
        }

        @Test
        void deveLancarExcecao_QuandoCpfJaCadastrado() {
            stubUsuario(gerente);
            when(funcionarioInterface.findByCpf("12345678909")).thenReturn(Optional.of(new EFuncionario()));

            assertThatThrownBy(() -> sFolhaPagamento.cadastrarFuncionario(dtoValido(), gerente.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Já existe um funcionário");
        }

        @Test
        void deveLancarExcecao_QuandoCargoInvalido() {
            stubUsuario(gerente);
            when(funcionarioInterface.findByCpf("12345678909")).thenReturn(Optional.empty());

            FuncionarioCadastroDto dto = dtoValido();
            dto.setCargo("CARGO_INEXISTENTE");

            assertThatThrownBy(() -> sFolhaPagamento.cadastrarFuncionario(dto, gerente.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cargo inválido");
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioSemPermissaoGerencial() {
            EUsuario financeiro = new EUsuario();
            financeiro.setEmail("financeiro@fazenda.com");
            financeiro.setPerfil(EnPerfilUsuario.FINANCEIRO);
            stubUsuario(financeiro);

            assertThatThrownBy(() -> sFolhaPagamento.cadastrarFuncionario(dtoValido(), financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Apenas Administrador ou Gerente");
        }
    }

    @Nested
    class AtualizarFuncionarioTests {

        @Test
        void deveAtualizarCamposInformados() {
            stubUsuario(gerente);
            EFuncionario funcionario = funcionario();
            when(funcionarioInterface.findById(1L)).thenReturn(Optional.of(funcionario));
            when(funcionarioInterface.save(any(EFuncionario.class))).thenAnswer(inv -> inv.getArgument(0));

            FuncionarioPutDto dto = new FuncionarioPutDto();
            dto.setSalarioBase(new BigDecimal("3500.00"));

            FuncionarioRespostaDto resultado = sFolhaPagamento.atualizarFuncionario(1L, dto, gerente.getEmail());

            assertThat(resultado.getSalarioBase()).isEqualByComparingTo("3500.00");
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoFuncionarioNaoExiste() {
            stubUsuario(gerente);
            when(funcionarioInterface.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sFolhaPagamento.atualizarFuncionario(99L, new FuncionarioPutDto(), gerente.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class LancarPagamentoTests {

        private PagamentoFuncionarioCadastroDto dtoValido(LocalDate dataPagamento) {
            PagamentoFuncionarioCadastroDto dto = new PagamentoFuncionarioCadastroDto();
            dto.setFuncionarioId(1L);
            dto.setAnoReferencia(2026);
            dto.setMesReferencia(3);
            dto.setDataPagamento(dataPagamento);
            return dto;
        }

        @Test
        void deveLancarPagamentoPago_CalculandoDescontosEEncargos_EGerandoLancamento() {
            stubUsuario(gerente);
            EFuncionario funcionario = funcionario();
            when(funcionarioInterface.findById(1L)).thenReturn(Optional.of(funcionario));
            when(pagamentoInterface.findByFuncionarioIdAndAnoReferenciaAndMesReferencia(1L, 2026, 3))
                    .thenReturn(Optional.empty());
            when(pagamentoInterface.save(any(EPagamentoFuncionario.class))).thenAnswer(inv -> inv.getArgument(0));

            PagamentoFuncionarioRespostaDto resultado =
                    sFolhaPagamento.lancarPagamento(dtoValido(LocalDate.now()), gerente.getEmail());

            assertThat(resultado.getStatusPagamento()).isEqualTo(EnStatusDespesa.PAGO);
            assertThat(resultado.getDescontoInss()).isEqualByComparingTo("270.00");
            assertThat(resultado.getEncargoFgts()).isEqualByComparingTo("240.00");
            assertThat(resultado.getValorBeneficios()).isEqualByComparingTo("350.00");
            assertThat(resultado.getValorLiquido()).isEqualByComparingTo("2730.00");
            verify(lancamentoFinanceiroService).registrarSaidaFolhaPagamento(any());
        }

        @Test
        void deveLancarPagamentoPendente_SemGerarLancamento_QuandoDataFutura() {
            stubUsuario(gerente);
            EFuncionario funcionario = funcionario();
            when(funcionarioInterface.findById(1L)).thenReturn(Optional.of(funcionario));
            when(pagamentoInterface.findByFuncionarioIdAndAnoReferenciaAndMesReferencia(1L, 2026, 3))
                    .thenReturn(Optional.empty());
            when(pagamentoInterface.save(any(EPagamentoFuncionario.class))).thenAnswer(inv -> inv.getArgument(0));

            PagamentoFuncionarioRespostaDto resultado = sFolhaPagamento.lancarPagamento(
                    dtoValido(LocalDate.now().plusDays(10)), gerente.getEmail());

            assertThat(resultado.getStatusPagamento()).isEqualTo(EnStatusDespesa.PENDENTE);
            verifyNoInteractions(lancamentoFinanceiroService);
        }

        @Test
        void deveLancarExcecao_QuandoJaExistePagamentoNoMes() {
            stubUsuario(gerente);
            when(funcionarioInterface.findById(1L)).thenReturn(Optional.of(funcionario()));
            when(pagamentoInterface.findByFuncionarioIdAndAnoReferenciaAndMesReferencia(1L, 2026, 3))
                    .thenReturn(Optional.of(new EPagamentoFuncionario()));

            assertThatThrownBy(() -> sFolhaPagamento.lancarPagamento(dtoValido(LocalDate.now()), gerente.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Já existe um pagamento");
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoFuncionarioNaoExiste() {
            stubUsuario(gerente);
            when(funcionarioInterface.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sFolhaPagamento.lancarPagamento(dtoValido(LocalDate.now()), gerente.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class EstornarPagamentoTests {

        private EPagamentoFuncionario pagamentoPago() {
            EPagamentoFuncionario pagamento = new EPagamentoFuncionario();
            pagamento.setId(1L);
            pagamento.setFuncionario(funcionario());
            pagamento.setStatusPagamento(EnStatusDespesa.PAGO);
            pagamento.setEstornado(false);
            return pagamento;
        }

        @Test
        void deveEstornarPagamentoPago_RevertendoLancamento() {
            stubUsuario(gerente);
            EPagamentoFuncionario pagamento = pagamentoPago();
            when(pagamentoInterface.findById(1L)).thenReturn(Optional.of(pagamento));
            when(pagamentoInterface.save(any(EPagamentoFuncionario.class))).thenAnswer(inv -> inv.getArgument(0));

            EstornoPagamentoDto dto = new EstornoPagamentoDto();
            dto.setMotivoEstorno("Pagamento em duplicidade");

            PagamentoFuncionarioRespostaDto resultado = sFolhaPagamento.estornarPagamento(1L, dto, gerente.getEmail());

            assertThat(resultado.getEstornado()).isTrue();
            verify(lancamentoFinanceiroService).estornarSaidaFolhaPagamento(pagamento);
        }

        @Test
        void deveEstornarPagamentoPendente_SemChamarEstornoDeLancamento() {
            stubUsuario(gerente);
            EPagamentoFuncionario pagamento = pagamentoPago();
            pagamento.setStatusPagamento(EnStatusDespesa.PENDENTE);
            when(pagamentoInterface.findById(1L)).thenReturn(Optional.of(pagamento));
            when(pagamentoInterface.save(any(EPagamentoFuncionario.class))).thenAnswer(inv -> inv.getArgument(0));

            EstornoPagamentoDto dto = new EstornoPagamentoDto();
            dto.setMotivoEstorno("Cancelado antes do pagamento");

            sFolhaPagamento.estornarPagamento(1L, dto, gerente.getEmail());

            verifyNoInteractions(lancamentoFinanceiroService);
        }

        @Test
        void deveLancarExcecao_QuandoJaEstornado() {
            stubUsuario(gerente);
            EPagamentoFuncionario pagamento = pagamentoPago();
            pagamento.setEstornado(true);
            when(pagamentoInterface.findById(1L)).thenReturn(Optional.of(pagamento));

            assertThatThrownBy(() -> sFolhaPagamento.estornarPagamento(1L, new EstornoPagamentoDto(), gerente.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("já foi estornado");
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoPagamentoNaoExiste() {
            stubUsuario(gerente);
            when(pagamentoInterface.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sFolhaPagamento.estornarPagamento(99L, new EstornoPagamentoDto(), gerente.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ListarFuncionariosTests {

        @Test
        void deveListarFuncionariosAtivos() {
            stubUsuario(gerente);
            when(funcionarioInterface.findByStatusOrderByNomeCompletoAsc(EnStatus.A))
                    .thenReturn(java.util.List.of(funcionario()));

            assertThat(sFolhaPagamento.listarFuncionarios(gerente.getEmail())).hasSize(1);
        }
    }
}
