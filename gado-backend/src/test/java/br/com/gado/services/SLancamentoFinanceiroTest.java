package br.com.gado.services;

import br.com.gado.dto.lancamentoFinanceiroDto.LancamentoFinanceiroRespostaDto;
import br.com.gado.dto.lancamentoFinanceiroDto.ResumoMensalDto;
import br.com.gado.entities.EConsumoInsumo;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.ELancamentoFinanceiro;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.ESobraAlimentacao;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnOrigemLancamentoFinanceiro;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoMovimentoFinanceiro;
import br.com.gado.repositories.IDocumentoSaida;
import br.com.gado.repositories.ILancamentoFinanceiro;
import br.com.gado.repositories.IUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SLancamentoFinanceiroTest {

    @InjectMocks
    private SLancamentoFinanceiro sLancamentoFinanceiro;

    @Mock private ILancamentoFinanceiro lancamentoInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private IDocumentoSaida documentoSaidaInterface;

    private EUsuario financeiro;
    private EUsuario cuidador;

    @BeforeEach
    void setUp() {
        financeiro = new EUsuario();
        financeiro.setEmail("financeiro@fazenda.com");
        financeiro.setPerfil(EnPerfilUsuario.FINANCEIRO);

        cuidador = new EUsuario();
        cuidador.setEmail("cuidador@fazenda.com");
        cuidador.setPerfil(EnPerfilUsuario.CUIDADOR);
    }

    private ESobraAlimentacao sobraNaoReaproveitada() {
        ESetor setor = new ESetor();
        setor.setNome("Pasto 1");
        EInsumo insumo = new EInsumo();
        insumo.setNome("Ração");
        insumo.setPrecoCompraMedio(5.0);
        EConsumoInsumo consumo = new EConsumoInsumo();
        consumo.setInsumo(insumo);
        consumo.setSetor(setor);

        ESobraAlimentacao sobra = new ESobraAlimentacao();
        sobra.setId(700L);
        sobra.setConsumoInsumo(consumo);
        sobra.setQuantidadeSobraUnidadePrimaria(2.0);
        sobra.setDataRegistro(LocalDateTime.of(2026, 3, 10, 8, 0));
        sobra.setRegistradoPorEmail("cuidador@fazenda.com");
        return sobra;
    }

    @Nested
    class ContabilizarPerdaAlimentacaoTests {

        @Test
        void deveCriarNovoLancamento_QuandoNaoExisteAindaParaEssaSobra() {
            ESobraAlimentacao sobra = sobraNaoReaproveitada();
            when(lancamentoInterface.findByOrigemAndOrigemId(EnOrigemLancamentoFinanceiro.PERDA_ALIMENTACAO, 700L))
                    .thenReturn(Optional.empty());
            when(lancamentoInterface.save(any(ELancamentoFinanceiro.class))).thenAnswer(inv -> inv.getArgument(0));

            sLancamentoFinanceiro.contabilizarPerdaAlimentacao(sobra);

            ArgumentCaptor<ELancamentoFinanceiro> captor = ArgumentCaptor.forClass(ELancamentoFinanceiro.class);
            verify(lancamentoInterface).save(captor.capture());
            ELancamentoFinanceiro lancamento = captor.getValue();

            assertThat(lancamento.getTipoMovimento()).isEqualTo(EnTipoMovimentoFinanceiro.SAIDA);
            assertThat(lancamento.getNaturezaFinanceira()).isEqualTo(EnNaturezaFinanceira.GASTO);
            assertThat(lancamento.getOrigem()).isEqualTo(EnOrigemLancamentoFinanceiro.PERDA_ALIMENTACAO);
            assertThat(lancamento.getOrigemId()).isEqualTo(700L);
            assertThat(lancamento.getValor()).isEqualByComparingTo(new BigDecimal("10.0"));
            assertThat(lancamento.getVirtual()).isTrue();
            assertThat(lancamento.getDataCompetencia()).isEqualTo(LocalDate.of(2026, 3, 10));
            assertThat(lancamento.getCriadoPorEmail()).isEqualTo("cuidador@fazenda.com");
        }

        @Test
        void deveAtualizarLancamentoExistente_EmVezDeCriarNovo() {
            ESobraAlimentacao sobra = sobraNaoReaproveitada();
            ELancamentoFinanceiro existente = new ELancamentoFinanceiro();
            existente.setId(555L);
            when(lancamentoInterface.findByOrigemAndOrigemId(EnOrigemLancamentoFinanceiro.PERDA_ALIMENTACAO, 700L))
                    .thenReturn(Optional.of(existente));
            when(lancamentoInterface.save(any(ELancamentoFinanceiro.class))).thenAnswer(inv -> inv.getArgument(0));

            sLancamentoFinanceiro.contabilizarPerdaAlimentacao(sobra);

            ArgumentCaptor<ELancamentoFinanceiro> captor = ArgumentCaptor.forClass(ELancamentoFinanceiro.class);
            verify(lancamentoInterface).save(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(555L);
        }

        @Test
        void deveUsarValorZero_QuandoInsumoSemPrecoCompraMedio() {
            ESobraAlimentacao sobra = sobraNaoReaproveitada();
            sobra.getConsumoInsumo().getInsumo().setPrecoCompraMedio(null);
            when(lancamentoInterface.findByOrigemAndOrigemId(any(), any())).thenReturn(Optional.empty());
            when(lancamentoInterface.save(any(ELancamentoFinanceiro.class))).thenAnswer(inv -> inv.getArgument(0));

            sLancamentoFinanceiro.contabilizarPerdaAlimentacao(sobra);

            ArgumentCaptor<ELancamentoFinanceiro> captor = ArgumentCaptor.forClass(ELancamentoFinanceiro.class);
            verify(lancamentoInterface).save(captor.capture());
            assertThat(captor.getValue().getValor()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    class EstornarPerdaAlimentacaoTests {

        @Test
        void deveExcluirLancamento_QuandoExiste() {
            ELancamentoFinanceiro lancamento = new ELancamentoFinanceiro();
            when(lancamentoInterface.findByOrigemAndOrigemId(EnOrigemLancamentoFinanceiro.PERDA_ALIMENTACAO, 700L))
                    .thenReturn(Optional.of(lancamento));

            sLancamentoFinanceiro.estornarPerdaAlimentacao(700L);

            verify(lancamentoInterface).delete(lancamento);
        }

        @Test
        void naoDeveLancarExcecao_QuandoNaoExisteLancamentoParaEstornar() {
            when(lancamentoInterface.findByOrigemAndOrigemId(EnOrigemLancamentoFinanceiro.PERDA_ALIMENTACAO, 700L))
                    .thenReturn(Optional.empty());

            sLancamentoFinanceiro.estornarPerdaAlimentacao(700L);

            verify(lancamentoInterface, never()).delete(any());
        }
    }

    @Nested
    class RegistrarLancamentoManualTests {

        @Test
        void deveRegistrarEntrada_SemNaturezaFinanceira() {
            when(usuarioInterface.findByEmailAndStatus(financeiro.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(financeiro));
            when(lancamentoInterface.save(any(ELancamentoFinanceiro.class))).thenAnswer(inv -> inv.getArgument(0));

            LancamentoFinanceiroRespostaDto resultado = sLancamentoFinanceiro.registrarLancamentoManual(
                    EnTipoMovimentoFinanceiro.ENTRADA, null, "Venda avulsa", new BigDecimal("100.00"),
                    LocalDate.of(2026, 3, 1), financeiro.getEmail());

            assertThat(resultado.getTipoMovimento()).isEqualTo(EnTipoMovimentoFinanceiro.ENTRADA);
            assertThat(resultado.getValor()).isEqualByComparingTo("100.00");
        }

        @Test
        void deveRegistrarSaida_ComNaturezaFinanceira() {
            when(usuarioInterface.findByEmailAndStatus(financeiro.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(financeiro));
            when(lancamentoInterface.save(any(ELancamentoFinanceiro.class))).thenAnswer(inv -> inv.getArgument(0));

            LancamentoFinanceiroRespostaDto resultado = sLancamentoFinanceiro.registrarLancamentoManual(
                    EnTipoMovimentoFinanceiro.SAIDA, EnNaturezaFinanceira.GASTO, "Manutenção",
                    new BigDecimal("50.00"), LocalDate.of(2026, 3, 1), financeiro.getEmail());

            assertThat(resultado.getNaturezaFinanceira()).isEqualTo(EnNaturezaFinanceira.GASTO);
        }

        @Test
        void deveLancarExcecao_QuandoEntradaComNaturezaInformada() {
            when(usuarioInterface.findByEmailAndStatus(financeiro.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(financeiro));

            assertThatThrownBy(() -> sLancamentoFinanceiro.registrarLancamentoManual(
                    EnTipoMovimentoFinanceiro.ENTRADA, EnNaturezaFinanceira.CUSTO, "x",
                    BigDecimal.TEN, LocalDate.now(), financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não é classificada como Custo nem Despesa");
        }

        @Test
        void deveLancarExcecao_QuandoSaidaSemNatureza() {
            when(usuarioInterface.findByEmailAndStatus(financeiro.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(financeiro));

            assertThatThrownBy(() -> sLancamentoFinanceiro.registrarLancamentoManual(
                    EnTipoMovimentoFinanceiro.SAIDA, null, "x", BigDecimal.TEN, LocalDate.now(), financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("precisa ser classificada como CUSTO ou GASTO");
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioSemPerfilDoModulo() {
            when(usuarioInterface.findByEmailAndStatus(cuidador.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(cuidador));

            assertThatThrownBy(() -> sLancamentoFinanceiro.registrarLancamentoManual(
                    EnTipoMovimentoFinanceiro.ENTRADA, null, "x", BigDecimal.TEN, LocalDate.now(), cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Administrador, Gerente ou Financeiro");
        }

        @Test
        void deveLancarExcecao_QuandoEmailEmBranco() {
            assertThatThrownBy(() -> sLancamentoFinanceiro.registrarLancamentoManual(
                    EnTipoMovimentoFinanceiro.ENTRADA, null, "x", BigDecimal.TEN, LocalDate.now(), " "))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(lancamentoInterface);
        }
    }

    @Nested
    class GerarResumoMensalTests {

        @Test
        void deveCalcularTotaisELucroLiquido() {
            when(usuarioInterface.findByEmailAndStatus(financeiro.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(financeiro));
            when(lancamentoInterface.somarPorBloco(2026, 3, EnTipoMovimentoFinanceiro.ENTRADA, null))
                    .thenReturn(new BigDecimal("1000.00"));
            when(lancamentoInterface.somarPorBlocoExcluindoOrigem(2026, 3, EnTipoMovimentoFinanceiro.SAIDA,
                    EnNaturezaFinanceira.CUSTO, EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE))
                    .thenReturn(new BigDecimal("300.00"));
            when(lancamentoInterface.somarPorBlocoExcluindoOrigem(2026, 3, EnTipoMovimentoFinanceiro.SAIDA,
                    EnNaturezaFinanceira.GASTO, EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE))
                    .thenReturn(new BigDecimal("200.00"));

            ResumoMensalDto resultado = sLancamentoFinanceiro.gerarResumoMensal(2026, 3, financeiro.getEmail());

            assertThat(resultado.getTotalEntradas()).isEqualByComparingTo("1000.00");
            assertThat(resultado.getTotalSaidas()).isEqualByComparingTo("500.00");
            assertThat(resultado.getLucroLiquido()).isEqualByComparingTo("500.00");
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioSemPermissao() {
            when(usuarioInterface.findByEmailAndStatus(cuidador.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(cuidador));

            assertThatThrownBy(() -> sLancamentoFinanceiro.gerarResumoMensal(2026, 3, cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(lancamentoInterface);
        }
    }

    @Nested
    class ListarPorBlocoTests {

        @Test
        void deveListarLancamentosDoBloco() {
            ELancamentoFinanceiro lancamento = new ELancamentoFinanceiro();
            lancamento.setId(1L);
            lancamento.setDescricao("Teste");
            when(usuarioInterface.findByEmailAndStatus(financeiro.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(financeiro));
            when(lancamentoInterface.findByAnoCompetenciaAndMesCompetenciaOrderByDataCompetenciaAsc(2026, 3))
                    .thenReturn(List.of(lancamento));

            List<LancamentoFinanceiroRespostaDto> resultado =
                    sLancamentoFinanceiro.listarPorBloco(2026, 3, financeiro.getEmail());

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getDescricao()).isEqualTo("Teste");
        }
    }
}
