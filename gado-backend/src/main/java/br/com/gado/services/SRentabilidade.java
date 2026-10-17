package br.com.gado.services;

import br.com.gado.dto.lancamentoFinanceiroDto.ResumoMensalDto;
import br.com.gado.dto.rentabilidadeDto.ContaParceiroDto;
import br.com.gado.dto.rentabilidadeDto.GastoGrupoDto;
import br.com.gado.dto.rentabilidadeDto.ProdutoVendidoDto;
import br.com.gado.dto.rentabilidadeDto.ResumoPeriodoDto;
import br.com.gado.entities.EDocumentoEntrada;
import br.com.gado.entities.EDocumentoEntradaItem;
import br.com.gado.entities.EDocumentoSaida;
import br.com.gado.entities.ELancamentoFinanceiro;
import br.com.gado.entities.EParceiro;
import br.com.gado.entities.EUsuario;
import br.com.gado.entities.EVendaAnimalItem;
import br.com.gado.entities.EVendaLeiteItem;
import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnOrigemLancamentoFinanceiro;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoDocumentoSaida;
import br.com.gado.enums.EnTipoMovimentoFinanceiro;
import br.com.gado.repositories.IDocumentoEntrada;
import br.com.gado.repositories.IDocumentoEntradaItem;
import br.com.gado.repositories.IDocumentoSaida;
import br.com.gado.repositories.ILancamentoFinanceiro;
import br.com.gado.repositories.IUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Agregações da aba "Rentabilidade" do Financeiro: resumo do DRE para um período livre (não só
 * mês/ano fixo, como em SLancamentoFinanceiro.gerarResumoMensal), contas a pagar/receber
 * resumidas por parceiro, vendas por produto (raça/leite) e maiores gastos por grupo.
 */
@Service
public class SRentabilidade {

    private static final Set<EnPerfilUsuario> PERFIS_MODULO =
            EnumSet.of(EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE, EnPerfilUsuario.FINANCEIRO);

    @Autowired
    private ILancamentoFinanceiro lancamentoInterface;

    @Autowired
    private IDocumentoEntrada documentoEntradaInterface;

    @Autowired
    private IDocumentoSaida documentoSaidaInterface;

    @Autowired
    private IDocumentoEntradaItem documentoEntradaItemInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private SLancamentoFinanceiro lancamentoFinanceiroService;

    private void validaAcessoModulo(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (!PERFIS_MODULO.contains(usuario.getPerfil())) {
            throw new IllegalArgumentException(
                    "Apenas Administrador, Gerente ou Financeiro podem acessar o módulo financeiro.");
        }
    }

    private void validarPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Informe a data de início e a data de fim do período.");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de fim não pode ser anterior à data de início.");
        }
    }

    // ── DRE do período ────────────────────────────────────────────────────

    @Transactional
    public ResumoPeriodoDto gerarResumoPeriodo(LocalDate dataInicio, LocalDate dataFim, String emailUsuario) {
        validaAcessoModulo(emailUsuario);
        validarPeriodo(dataInicio, dataFim);

        BigDecimal totalEntradas = lancamentoInterface.somarPorPeriodoExcluindoOrigem(
                dataInicio, dataFim, EnTipoMovimentoFinanceiro.ENTRADA, null, null);
        BigDecimal totalSaidasCusto = lancamentoInterface.somarPorPeriodoExcluindoOrigem(
                dataInicio, dataFim, EnTipoMovimentoFinanceiro.SAIDA, EnNaturezaFinanceira.CUSTO,
                EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE);
        BigDecimal totalSaidasDespesa = lancamentoInterface.somarPorPeriodoExcluindoOrigem(
                dataInicio, dataFim, EnTipoMovimentoFinanceiro.SAIDA, EnNaturezaFinanceira.GASTO,
                EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE);
        BigDecimal totalSaidas = totalSaidasCusto.add(totalSaidasDespesa);
        BigDecimal lucroLiquido = totalEntradas.subtract(totalSaidas);

        List<ResumoMensalDto> porMes = new ArrayList<>();
        LocalDate competencia = dataInicio.withDayOfMonth(1);
        LocalDate limite = dataFim.withDayOfMonth(1);
        while (!competencia.isAfter(limite)) {
            porMes.add(lancamentoFinanceiroService.gerarResumoMensalInterno(
                    competencia.getYear(), competencia.getMonthValue()));
            competencia = competencia.plusMonths(1);
        }

        return new ResumoPeriodoDto(dataInicio, dataFim, totalEntradas, totalSaidasCusto, totalSaidasDespesa,
                totalSaidas, lucroLiquido, porMes);
    }

    // ── Contas a pagar/receber por parceiro ───────────────────────────────

    /** Agrupa por parceiro as NFs (entrada = a pagar, saída = a receber) com vencimento no período. */
    @Transactional
    public List<ContaParceiroDto> listarContasPorParceiro(LocalDate dataInicio, LocalDate dataFim, String emailUsuario) {
        validaAcessoModulo(emailUsuario);
        validarPeriodo(dataInicio, dataFim);

        Map<Long, ContaParceiroDto> porParceiro = new LinkedHashMap<>();

        List<EDocumentoEntrada> aPagar = documentoEntradaInterface.findByStatusAndDataVencimentoBetween(
                EnStatus.A, dataInicio, dataFim);
        for (EDocumentoEntrada documento : aPagar) {
            EParceiro parceiro = documento.getFornecedor();
            if (parceiro == null) {
                continue;
            }
            ContaParceiroDto conta = porParceiro.computeIfAbsent(parceiro.getId(),
                    id -> new ContaParceiroDto(parceiro.getId(), parceiro.getNome(),
                            BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, null));
            conta.setTotalAPagar(conta.getTotalAPagar().add(documento.getValorTotal()));
            conta.setQtdNfsPagar(conta.getQtdNfsPagar() + 1);
            conta.setProximoVencimento(menorData(conta.getProximoVencimento(), documento.getDataVencimento()));
        }

        List<EDocumentoSaida> aReceber = documentoSaidaInterface.findByStatusAndDataVencimentoBetween(
                EnStatus.A, dataInicio, dataFim);
        for (EDocumentoSaida documento : aReceber) {
            EParceiro parceiro = documento.getComprador();
            if (parceiro == null) {
                continue;
            }
            ContaParceiroDto conta = porParceiro.computeIfAbsent(parceiro.getId(),
                    id -> new ContaParceiroDto(parceiro.getId(), parceiro.getNome(),
                            BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, null));
            conta.setTotalAReceber(conta.getTotalAReceber().add(documento.getValorTotal()));
            conta.setQtdNfsReceber(conta.getQtdNfsReceber() + 1);
            conta.setProximoVencimento(menorData(conta.getProximoVencimento(), documento.getDataVencimento()));
        }

        return porParceiro.values().stream()
                .sorted(Comparator.comparing((ContaParceiroDto c) -> c.getTotalAPagar().add(c.getTotalAReceber()))
                        .reversed())
                .collect(Collectors.toList());
    }

    private LocalDate menorData(LocalDate atual, LocalDate novaData) {
        if (atual == null) return novaData;
        if (novaData == null) return atual;
        return atual.isBefore(novaData) ? atual : novaData;
    }

    // ── Vendas por produto (raça de animal + leite) ───────────────────────

    /** Vendas confirmadas no período (por data de emissão), agrupadas por raça + um item agregado "Leite". */
    @Transactional
    public List<ProdutoVendidoDto> listarVendasPorProduto(LocalDate dataInicio, LocalDate dataFim, String emailUsuario) {
        validaAcessoModulo(emailUsuario);
        validarPeriodo(dataInicio, dataFim);

        List<EDocumentoSaida> vendas = documentoSaidaInterface.findByStatusAndDataEmissaoBetween(
                EnStatus.A, dataInicio, dataFim);

        BigDecimal litrosLeite = BigDecimal.ZERO;
        BigDecimal valorLeite = BigDecimal.ZERO;
        Map<String, BigDecimal[]> porRaca = new LinkedHashMap<>(); // [quantidade, valor]

        for (EDocumentoSaida venda : vendas) {
            if (venda.getTipoDocumento() == EnTipoDocumentoSaida.VENDA_LEITE) {
                for (EVendaLeiteItem item : venda.getItensLeite()) {
                    litrosLeite = litrosLeite.add(item.getLitros());
                    valorLeite = valorLeite.add(item.getValorTotal());
                }
            } else {
                for (EVendaAnimalItem item : venda.getItensAnimal()) {
                    String raca = item.getAnimal().getRaca() != null ? item.getAnimal().getRaca().getNome() : "Sem raça";
                    BigDecimal[] acumulado = porRaca.computeIfAbsent(raca, r -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                    acumulado[0] = acumulado[0].add(BigDecimal.ONE);
                    acumulado[1] = acumulado[1].add(item.getValorVenda());
                }
            }
        }

        List<ProdutoVendidoDto> resultado = new ArrayList<>();
        if (litrosLeite.compareTo(BigDecimal.ZERO) > 0) {
            resultado.add(new ProdutoVendidoDto("Leite", litrosLeite, "litro(s)", valorLeite));
        }
        for (Map.Entry<String, BigDecimal[]> entrada : porRaca.entrySet()) {
            resultado.add(new ProdutoVendidoDto(entrada.getKey(), entrada.getValue()[0], "cabeça(s)", entrada.getValue()[1]));
        }

        resultado.sort(Comparator.comparing(ProdutoVendidoDto::getValorTotal).reversed());
        return resultado;
    }

    // ── Maiores gastos por grupo ───────────────────────────────────────────

    /**
     * Agrupa as saídas financeiras do período (excluindo CONSUMO_ESTOQUE, já contado na compra —
     * mesma exclusão de SLancamentoFinanceiro.gerarResumoMensal) por grupo: "Folha de Pagamento"
     * para FOLHA_PAGAMENTO, o grupoProduto do item para DOCUMENTO_ENTRADA, e um rótulo fixo para
     * PERDA_ALIMENTACAO/MANUAL.
     */
    @Transactional
    public List<GastoGrupoDto> listarGastosPorGrupo(LocalDate dataInicio, LocalDate dataFim, String emailUsuario) {
        validaAcessoModulo(emailUsuario);
        validarPeriodo(dataInicio, dataFim);

        List<ELancamentoFinanceiro> saidas = lancamentoInterface.findByTipoMovimentoAndDataCompetenciaBetween(
                EnTipoMovimentoFinanceiro.SAIDA, dataInicio, dataFim);

        Map<String, BigDecimal> porGrupo = new LinkedHashMap<>();
        for (ELancamentoFinanceiro lancamento : saidas) {
            if (lancamento.getOrigem() == EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE) {
                continue;
            }
            String grupo = resolverGrupoGasto(lancamento);
            porGrupo.merge(grupo, lancamento.getValor(), BigDecimal::add);
        }

        return porGrupo.entrySet().stream()
                .map(e -> new GastoGrupoDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(GastoGrupoDto::getValorTotal).reversed())
                .collect(Collectors.toList());
    }

    private String resolverGrupoGasto(ELancamentoFinanceiro lancamento) {
        switch (lancamento.getOrigem()) {
            case FOLHA_PAGAMENTO:
                return "Folha de Pagamento";
            case DOCUMENTO_ENTRADA:
                return documentoEntradaItemInterface.findById(lancamento.getOrigemId())
                        .map(EDocumentoEntradaItem::getProduto)
                        .map(produto -> produto.getGrupoProduto() != null
                                ? produto.getGrupoProduto().getNome() : "Administrativo/Outros")
                        .orElse("Administrativo/Outros");
            case PERDA_ALIMENTACAO:
                return "Perda de Alimentação";
            case MANUAL:
                return "Lançamentos Manuais";
            default:
                return "Outros";
        }
    }
}
