package br.com.gado.services;

import br.com.gado.dto.lancamentoFinanceiroDto.ResumoMensalDto;
import br.com.gado.dto.rentabilidadeDto.ContaParceiroDto;
import br.com.gado.dto.rentabilidadeDto.GastoGrupoDto;
import br.com.gado.dto.rentabilidadeDto.ProdutoVendidoDto;
import br.com.gado.dto.rentabilidadeDto.ResumoPeriodoDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EDocumentoEntrada;
import br.com.gado.entities.EDocumentoEntradaItem;
import br.com.gado.entities.EDocumentoSaida;
import br.com.gado.entities.EGrupoProduto;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.ELancamentoFinanceiro;
import br.com.gado.entities.ELote;
import br.com.gado.entities.EParceiro;
import br.com.gado.entities.ERaca;
import br.com.gado.entities.EUsuario;
import br.com.gado.entities.EVendaAnimalItem;
import br.com.gado.entities.EVendaLeiteItem;
import br.com.gado.enums.EnOrigemLancamentoFinanceiro;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnStatusAnimal;
import br.com.gado.enums.EnTipoDocumentoSaida;
import br.com.gado.enums.EnTipoMovimentoFinanceiro;
import br.com.gado.repositories.IDocumentoEntrada;
import br.com.gado.repositories.IDocumentoEntradaItem;
import br.com.gado.repositories.IDocumentoSaida;
import br.com.gado.repositories.ILancamentoFinanceiro;
import br.com.gado.repositories.IUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SRentabilidadeTest {

    @InjectMocks
    private SRentabilidade sRentabilidade;

    @Mock private ILancamentoFinanceiro lancamentoInterface;
    @Mock private IDocumentoEntrada documentoEntradaInterface;
    @Mock private IDocumentoSaida documentoSaidaInterface;
    @Mock private IDocumentoEntradaItem documentoEntradaItemInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private SLancamentoFinanceiro lancamentoFinanceiroService;

    private EUsuario financeiro;

    @BeforeEach
    void setUp() {
        financeiro = new EUsuario();
        financeiro.setEmail("financeiro@fazenda.com");
        financeiro.setPerfil(EnPerfilUsuario.FINANCEIRO);
        when(usuarioInterface.findByEmailAndStatus(financeiro.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(financeiro));
    }

    @Nested
    class GerarResumoPeriodoTests {

        @Test
        void deveSomarTotaisEMontarDetalhamentoMensal() {
            LocalDate inicio = LocalDate.of(2026, 1, 10);
            LocalDate fim = LocalDate.of(2026, 2, 20);

            when(lancamentoInterface.somarPorPeriodoExcluindoOrigem(inicio, fim, EnTipoMovimentoFinanceiro.ENTRADA, null, null))
                    .thenReturn(new BigDecimal("1000.00"));
            when(lancamentoInterface.somarPorPeriodoExcluindoOrigem(eq(inicio), eq(fim), eq(EnTipoMovimentoFinanceiro.SAIDA),
                    eq(br.com.gado.enums.EnNaturezaFinanceira.CUSTO), eq(EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE)))
                    .thenReturn(new BigDecimal("300.00"));
            when(lancamentoInterface.somarPorPeriodoExcluindoOrigem(eq(inicio), eq(fim), eq(EnTipoMovimentoFinanceiro.SAIDA),
                    eq(br.com.gado.enums.EnNaturezaFinanceira.GASTO), eq(EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE)))
                    .thenReturn(new BigDecimal("200.00"));

            when(lancamentoFinanceiroService.gerarResumoMensalInterno(2026, 1))
                    .thenReturn(new ResumoMensalDto(2026, 1, new BigDecimal("600"), new BigDecimal("150"),
                            new BigDecimal("100"), new BigDecimal("250"), new BigDecimal("350")));
            when(lancamentoFinanceiroService.gerarResumoMensalInterno(2026, 2))
                    .thenReturn(new ResumoMensalDto(2026, 2, new BigDecimal("400"), new BigDecimal("150"),
                            new BigDecimal("100"), new BigDecimal("250"), new BigDecimal("150")));

            ResumoPeriodoDto resultado = sRentabilidade.gerarResumoPeriodo(inicio, fim, financeiro.getEmail());

            assertThat(resultado.getTotalEntradas()).isEqualByComparingTo("1000.00");
            assertThat(resultado.getTotalSaidasCusto()).isEqualByComparingTo("300.00");
            assertThat(resultado.getTotalSaidasDespesa()).isEqualByComparingTo("200.00");
            assertThat(resultado.getTotalSaidas()).isEqualByComparingTo("500.00");
            assertThat(resultado.getLucroLiquido()).isEqualByComparingTo("500.00");
            assertThat(resultado.getPorMes()).hasSize(2);
            assertThat(resultado.getPorMes().get(0).getMes()).isEqualTo(1);
            assertThat(resultado.getPorMes().get(1).getMes()).isEqualTo(2);
        }

        @Test
        void deveLancarExcecao_QuandoFimAnteriorAoInicio() {
            assertThatThrownBy(() -> sRentabilidade.gerarResumoPeriodo(
                    LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1), financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("anterior");
        }
    }

    @Nested
    class ListarContasPorParceiroTests {

        @Test
        void deveAgruparPorParceiroESomarPagarEReceber() {
            LocalDate inicio = LocalDate.of(2026, 1, 1);
            LocalDate fim = LocalDate.of(2026, 1, 31);

            EParceiro fornecedor = new EParceiro();
            fornecedor.setId(1L);
            fornecedor.setNome("Fornecedor A");

            EDocumentoEntrada entrada1 = new EDocumentoEntrada();
            entrada1.setFornecedor(fornecedor);
            entrada1.setValorTotal(new BigDecimal("100.00"));
            entrada1.setDataVencimento(LocalDate.of(2026, 1, 10));

            EDocumentoEntrada entrada2 = new EDocumentoEntrada();
            entrada2.setFornecedor(fornecedor);
            entrada2.setValorTotal(new BigDecimal("50.00"));
            entrada2.setDataVencimento(LocalDate.of(2026, 1, 5));

            when(documentoEntradaInterface.findByStatusAndDataVencimentoBetween(EnStatus.A, inicio, fim))
                    .thenReturn(List.of(entrada1, entrada2));

            EParceiro comprador = new EParceiro();
            comprador.setId(2L);
            comprador.setNome("Comprador B");

            EDocumentoSaida saida = new EDocumentoSaida();
            saida.setComprador(comprador);
            saida.setValorTotal(new BigDecimal("500.00"));
            saida.setDataVencimento(LocalDate.of(2026, 1, 20));

            when(documentoSaidaInterface.findByStatusAndDataVencimentoBetween(EnStatus.A, inicio, fim))
                    .thenReturn(List.of(saida));

            List<ContaParceiroDto> resultado = sRentabilidade.listarContasPorParceiro(inicio, fim, financeiro.getEmail());

            assertThat(resultado).hasSize(2);
            // Comprador B tem o maior total (500 > 150), deve vir primeiro (ordem decrescente).
            assertThat(resultado.get(0).getParceiroNome()).isEqualTo("Comprador B");
            assertThat(resultado.get(0).getTotalAReceber()).isEqualByComparingTo("500.00");

            ContaParceiroDto fornecedorA = resultado.stream()
                    .filter(c -> c.getParceiroNome().equals("Fornecedor A")).findFirst().orElseThrow();
            assertThat(fornecedorA.getTotalAPagar()).isEqualByComparingTo("150.00");
            assertThat(fornecedorA.getQtdNfsPagar()).isEqualTo(2);
            assertThat(fornecedorA.getProximoVencimento()).isEqualTo(LocalDate.of(2026, 1, 5));
        }
    }

    @Nested
    class ListarVendasPorProdutoTests {

        @Test
        void deveAgruparPorRacaEIncluirLeite_OrdenadoDecrescente() {
            LocalDate inicio = LocalDate.of(2026, 1, 1);
            LocalDate fim = LocalDate.of(2026, 1, 31);

            EDocumentoSaida vendaLeite = new EDocumentoSaida();
            vendaLeite.setTipoDocumento(EnTipoDocumentoSaida.VENDA_LEITE);
            EVendaLeiteItem itemLeite = new EVendaLeiteItem();
            itemLeite.setLitros(new BigDecimal("1000"));
            itemLeite.setValorTotal(new BigDecimal("2000.00"));
            vendaLeite.setItensLeite(List.of(itemLeite));
            vendaLeite.setItensAnimal(List.of());

            EDocumentoSaida vendaAnimal = new EDocumentoSaida();
            vendaAnimal.setTipoDocumento(EnTipoDocumentoSaida.VENDA_ANIMAL);
            EAnimal animalNelore = new EAnimal();
            ERaca racaNelore = new ERaca();
            racaNelore.setNome("Nelore");
            animalNelore.setRaca(racaNelore);
            EVendaAnimalItem itemNelore = new EVendaAnimalItem();
            itemNelore.setAnimal(animalNelore);
            itemNelore.setDestino(EnStatusAnimal.VENDIDO);
            itemNelore.setValorVenda(new BigDecimal("5000.00"));
            vendaAnimal.setItensAnimal(List.of(itemNelore));
            vendaAnimal.setItensLeite(List.of());

            when(documentoSaidaInterface.findByStatusAndDataEmissaoBetween(EnStatus.A, inicio, fim))
                    .thenReturn(List.of(vendaLeite, vendaAnimal));

            List<ProdutoVendidoDto> resultado = sRentabilidade.listarVendasPorProduto(inicio, fim, financeiro.getEmail());

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getNome()).isEqualTo("Nelore");
            assertThat(resultado.get(0).getValorTotal()).isEqualByComparingTo("5000.00");
            assertThat(resultado.get(1).getNome()).isEqualTo("Leite");
            assertThat(resultado.get(1).getUnidade()).isEqualTo("litro(s)");
            assertThat(resultado.get(1).getQuantidade()).isEqualByComparingTo("1000");
        }
    }

    @Nested
    class ListarGastosPorGrupoTests {

        @Test
        void deveAgruparPorGrupoEExcluirConsumoEstoque() {
            LocalDate inicio = LocalDate.of(2026, 1, 1);
            LocalDate fim = LocalDate.of(2026, 1, 31);

            ELancamentoFinanceiro folha = new ELancamentoFinanceiro();
            folha.setOrigem(EnOrigemLancamentoFinanceiro.FOLHA_PAGAMENTO);
            folha.setValor(new BigDecimal("3000.00"));

            EGrupoProduto grupoRacao = new EGrupoProduto();
            grupoRacao.setNome("Ração");
            EInsumo produtoComGrupo = new EInsumo();
            produtoComGrupo.setGrupoProduto(grupoRacao);
            EDocumentoEntradaItem itemComProduto = new EDocumentoEntradaItem();
            itemComProduto.setId(10L);
            itemComProduto.setProduto(produtoComGrupo);
            when(documentoEntradaItemInterface.findById(10L)).thenReturn(Optional.of(itemComProduto));

            ELancamentoFinanceiro compraRacao = new ELancamentoFinanceiro();
            compraRacao.setOrigem(EnOrigemLancamentoFinanceiro.DOCUMENTO_ENTRADA);
            compraRacao.setOrigemId(10L);
            compraRacao.setValor(new BigDecimal("1500.00"));

            EDocumentoEntradaItem itemSemProduto = new EDocumentoEntradaItem();
            itemSemProduto.setId(11L);
            when(documentoEntradaItemInterface.findById(11L)).thenReturn(Optional.of(itemSemProduto));

            ELancamentoFinanceiro compraSemProduto = new ELancamentoFinanceiro();
            compraSemProduto.setOrigem(EnOrigemLancamentoFinanceiro.DOCUMENTO_ENTRADA);
            compraSemProduto.setOrigemId(11L);
            compraSemProduto.setValor(new BigDecimal("200.00"));

            ELancamentoFinanceiro consumo = new ELancamentoFinanceiro();
            consumo.setOrigem(EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE);
            consumo.setValor(new BigDecimal("9999.00"));

            when(lancamentoInterface.findByTipoMovimentoAndDataCompetenciaBetween(EnTipoMovimentoFinanceiro.SAIDA, inicio, fim))
                    .thenReturn(List.of(folha, compraRacao, compraSemProduto, consumo));

            List<GastoGrupoDto> resultado = sRentabilidade.listarGastosPorGrupo(inicio, fim, financeiro.getEmail());

            assertThat(resultado).extracting(GastoGrupoDto::getGrupoNome)
                    .containsExactly("Folha de Pagamento", "Ração", "Administrativo/Outros");
            assertThat(resultado.get(0).getValorTotal()).isEqualByComparingTo("3000.00");
            assertThat(resultado).noneMatch(g -> g.getValorTotal().compareTo(new BigDecimal("9999.00")) == 0);
        }
    }
}
