package br.com.gado.services;

import br.com.gado.dto.lancamentoFinanceiroDto.FontesReceitaDto;
import br.com.gado.dto.lancamentoFinanceiroDto.LancamentoFinanceiroRespostaDto;
import br.com.gado.dto.lancamentoFinanceiroDto.ResumoMensalDto;
import br.com.gado.entities.EConsumoEstoque;
import br.com.gado.entities.EConsumoEstoqueItem;
import br.com.gado.entities.EDocumentoEntradaItem;
import br.com.gado.entities.EDocumentoSaida;
import br.com.gado.entities.ELancamentoFinanceiro;
import br.com.gado.entities.EPagamentoFuncionario;
import br.com.gado.entities.EUsuario;
import br.com.gado.entities.EVendaAnimalItem;
import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnOrigemLancamentoFinanceiro;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoDocumentoSaida;
import br.com.gado.enums.EnTipoMovimentoFinanceiro;
import br.com.gado.repositories.IDocumentoSaida;
import br.com.gado.repositories.ILancamentoFinanceiro;
import br.com.gado.repositories.IUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Livro-razão do módulo financeiro (ELancamentoFinanceiro) e ponto único de geração do DRE.
 * Nenhum outro serviço grava saldo diretamente em ELancamentoFinanceiro — todos passam por aqui,
 * o que garante que Bloco Ano/Mês, sinal (ENTRADA/SAIDA) e Custo/Despesa fiquem sempre consistentes.
 *
 * Os métodos "registrarXxx" são de uso interno do módulo (pacote services) — são chamados pelos
 * outros serviços do domínio financeiro (e, no caso de contabilizarConsumoEstoque, pelo módulo de
 * Insumos) depois que a operação de origem já validou sua própria permissão. Só os métodos
 * expostos a um usuário final (gerarResumoMensal, listarPorBloco, registrarLancamentoManual)
 * re-validam o perfil de quem chama.
 */
@Service
public class SLancamentoFinanceiro {

    /** Espelha a regra "módulo inteiro só acessível a ADMINISTRADOR, GERENTE e FINANCEIRO". */
    private static final Set<EnPerfilUsuario> PERFIS_MODULO =
            EnumSet.of(EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE, EnPerfilUsuario.FINANCEIRO);

    @Autowired
    private ILancamentoFinanceiro lancamentoInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private IDocumentoSaida documentoSaidaInterface;

    // ── Permissões ───────────────────────────────────────────────────────

    private EUsuario resolveUsuarioModulo(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (!PERFIS_MODULO.contains(usuario.getPerfil())) {
            throw new IllegalArgumentException(
                    "Apenas Administrador, Gerente ou Financeiro podem acessar o módulo financeiro.");
        }
        return usuario;
    }

    // ── Escrita interna (chamada pelos outros serviços do domínio) ───────

    /**
     * Gera uma "Saída Financeira Virtual" para um item de consumo de estoque (ex: ração
     * consumida). Chamado pelo módulo de Insumos após SConsumoEstoque.registrarConsumo salvar
     * o consumo — próximo passo de integração é essa chamada dentro de SConsumoEstoque.
     * A natureza (CUSTO/GASTO) vem do grupoProduto do insumo consumido; sem grupo cadastrado,
     * assume GASTO por padrão (mais conservador para o DRE do que presumir CUSTO indevido).
     */
    @Transactional
    void contabilizarConsumoEstoque(EConsumoEstoque consumo) {
        for (EConsumoEstoqueItem item : consumo.getItens()) {
            EnNaturezaFinanceira natureza = item.getInsumo().getGrupoProduto() != null
                    ? item.getInsumo().getGrupoProduto().getNaturezaFinanceira()
                    : EnNaturezaFinanceira.GASTO;

            double precoUnitario = item.getInsumo().getPrecoCompraMedio() != null
                    ? item.getInsumo().getPrecoCompraMedio()
                    : 0.0;
            BigDecimal valor = BigDecimal.valueOf(item.getQuantidadeBaixaUnidadePrimaria())
                    .multiply(BigDecimal.valueOf(precoUnitario));

            String descricao = String.format("Consumo de estoque: %s (%s)",
                    item.getInsumo().getNome(), consumo.getMotivo());

            registrar(EnTipoMovimentoFinanceiro.SAIDA, natureza, EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE,
                    item.getId(), descricao, valor, consumo.getDataConsumo().toLocalDate(),
                    consumo.getCriadoPorEmail(), true);
        }
    }

    /**
     * Reverte a(s) Saída(s) Financeira(s) Virtual(is) geradas para um consumo de estoque.
     * Chamado por SConsumoEstoque.cancelarConsumo — sem isso, cancelar um consumo estornaria o
     * estoque mas deixaria um custo "fantasma" no DRE.
     */
    @Transactional
    void estornarSaidaConsumoEstoque(EConsumoEstoque consumo) {
        for (EConsumoEstoqueItem item : consumo.getItens()) {
            lancamentoInterface.findByOrigemAndOrigemId(EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE, item.getId())
                    .ifPresent(lancamentoInterface::delete);
        }
    }

    /**
     * Cria ou atualiza (upsert por origem+origemId) o lançamento de saída de um item de
     * documento de entrada. Chamado por SDocumentoEntrada sempre que um item passa a valer
     * para o DRE: na aprovação do documento, numa correção via editarNfe, ou numa reclassificação
     * de natureza por vincularProduto.
     */
    @Transactional
    void registrarOuAtualizarSaidaItemDocumento(EDocumentoEntradaItem item, String emailResponsavel) {
        EnNaturezaFinanceira natureza = item.getNaturezaFinanceira() != null
                ? item.getNaturezaFinanceira()
                : EnNaturezaFinanceira.GASTO;

        String descricao = String.format("%s %s - %s",
                item.getDocumentoEntrada().getTipoDocumento(),
                item.getDocumentoEntrada().getNumeroDocumento() != null
                        ? item.getDocumentoEntrada().getNumeroDocumento() : "s/nº",
                item.getDescricaoXml());

        registrar(EnTipoMovimentoFinanceiro.SAIDA, natureza, EnOrigemLancamentoFinanceiro.DOCUMENTO_ENTRADA,
                item.getId(), descricao, item.getValorTotal(), item.getDocumentoEntrada().getDataEmissao(),
                emailResponsavel, false);
    }

    /**
     * Contabiliza o custo total do funcionário (bruto + FGTS + benefícios — não apenas o líquido
     * pago) para o mês de referência. Chamado por SFolhaPagamento apenas quando o pagamento é
     * efetivamente PAGO (um pagamento PENDENTE ainda não deve compor o DRE).
     */
    @Transactional
    void registrarSaidaFolhaPagamento(EPagamentoFuncionario pagamento) {
        BigDecimal beneficios = pagamento.getValorBeneficios() != null
                ? pagamento.getValorBeneficios() : BigDecimal.ZERO;
        BigDecimal bonus = pagamento.getValorBonus() != null
                ? pagamento.getValorBonus() : BigDecimal.ZERO;
        BigDecimal custoTotal = pagamento.getValorBruto()
                .add(pagamento.getEncargoFgts())
                .add(beneficios)
                .add(bonus);

        String descricao = String.format("Folha de pagamento %02d/%d - %s",
                pagamento.getMesReferencia(), pagamento.getAnoReferencia(),
                pagamento.getFuncionario().getNomeCompleto());

        registrar(EnTipoMovimentoFinanceiro.SAIDA, pagamento.getNaturezaFinanceiraSnapshot(),
                EnOrigemLancamentoFinanceiro.FOLHA_PAGAMENTO, pagamento.getId(), descricao, custoTotal,
                LocalDate.of(pagamento.getAnoReferencia(), pagamento.getMesReferencia(), 1),
                pagamento.getFuncionario().getUsuario() != null
                        ? pagamento.getFuncionario().getUsuario().getEmail() : "sistema",
                false);
    }

    /**
     * Reverte a Saída Financeira gerada para um item de documento de entrada. Chamado por
     * SDocumentoEntrada.excluirDocumento ao excluir/estornar um documento já aprovado.
     */
    @Transactional
    void estornarSaidaItemDocumento(EDocumentoEntradaItem item) {
        lancamentoInterface.findByOrigemAndOrigemId(EnOrigemLancamentoFinanceiro.DOCUMENTO_ENTRADA, item.getId())
                .ifPresent(lancamentoInterface::delete);
    }

    /**
     * Reverte a Saída Financeira gerada para um pagamento de folha. Chamado por
     * SFolhaPagamento.estornarPagamento — sem isso, estornar um pagamento manteria o
     * custo lançado no DRE mesmo o dinheiro não tendo mais efeito (pagamento cancelado).
     */
    @Transactional
    void estornarSaidaFolhaPagamento(EPagamentoFuncionario pagamento) {
        lancamentoInterface.findByOrigemAndOrigemId(EnOrigemLancamentoFinanceiro.FOLHA_PAGAMENTO, pagamento.getId())
                .ifPresent(lancamentoInterface::delete);
    }

    /**
     * Receita de uma venda (leite ou animal/abate). Chamado por SDocumentoSaida após persistir
     * o EDocumentoSaida — origemId é o id do documento (um lançamento por venda, não por item).
     */
    @Transactional
    void registrarEntradaVenda(Long documentoSaidaId, String descricao, BigDecimal valor,
                                LocalDate dataCompetencia, String emailResponsavel) {
        registrar(EnTipoMovimentoFinanceiro.ENTRADA, null, EnOrigemLancamentoFinanceiro.VENDA,
                documentoSaidaId, descricao, valor, dataCompetencia, emailResponsavel, false);
    }

    /**
     * Lançamento manual, para movimentos ainda sem módulo de origem automatizado (ex: registrar
     * uma venda até o módulo de Vendas existir). Diferente dos métodos acima, é chamado
     * diretamente por um usuário do módulo — por isso valida o perfil aqui.
     */
    @Transactional
    public LancamentoFinanceiroRespostaDto registrarLancamentoManual(EnTipoMovimentoFinanceiro tipoMovimento,
                                                                       EnNaturezaFinanceira naturezaFinanceira,
                                                                       String descricao, BigDecimal valor,
                                                                       LocalDate dataCompetencia,
                                                                       String emailUsuario) {
        resolveUsuarioModulo(emailUsuario);
        if (tipoMovimento == EnTipoMovimentoFinanceiro.ENTRADA && naturezaFinanceira != null) {
            throw new IllegalArgumentException("Uma ENTRADA não é classificada como Custo nem Despesa.");
        }
        if (tipoMovimento == EnTipoMovimentoFinanceiro.SAIDA && naturezaFinanceira == null) {
            throw new IllegalArgumentException("Toda SAIDA precisa ser classificada como CUSTO ou GASTO.");
        }

        ELancamentoFinanceiro lancamento = registrar(tipoMovimento, naturezaFinanceira,
                EnOrigemLancamentoFinanceiro.MANUAL, null, descricao, valor, dataCompetencia,
                emailUsuario, false);
        return toRespostaDto(lancamento);
    }

    private ELancamentoFinanceiro registrar(EnTipoMovimentoFinanceiro tipoMovimento,
                                             EnNaturezaFinanceira naturezaFinanceira,
                                             EnOrigemLancamentoFinanceiro origem, Long origemId,
                                             String descricao, BigDecimal valor, LocalDate dataCompetencia,
                                             String criadoPorEmail, boolean virtual) {
        Optional<ELancamentoFinanceiro> existente = origemId != null
                ? lancamentoInterface.findByOrigemAndOrigemId(origem, origemId)
                : Optional.empty();
        ELancamentoFinanceiro lancamento = existente.orElseGet(ELancamentoFinanceiro::new);

        lancamento.setTipoMovimento(tipoMovimento);
        lancamento.setNaturezaFinanceira(naturezaFinanceira);
        lancamento.setOrigem(origem);
        lancamento.setOrigemId(origemId);
        lancamento.setDescricao(descricao);
        lancamento.setValor(valor);
        lancamento.setDataCompetencia(dataCompetencia);
        lancamento.setAnoCompetencia(dataCompetencia.getYear());
        lancamento.setMesCompetencia(dataCompetencia.getMonthValue());
        lancamento.setVirtual(virtual);
        lancamento.setCriadoPorEmail(criadoPorEmail);

        return lancamentoInterface.save(lancamento);
    }

    // ── Leitura / DRE ──────────────────────────────────────────────────

    /**
     * "Resumo Mensal" (DRE) do Bloco Ano/Mês informado: Total de Entradas, Total de Saídas
     * quebrado em Custo x Despesa, e Lucro Líquido (Entradas - Saídas).
     *
     * Despesa mensal é calculada pelo que entrou (compras via NF/recibo, origem
     * DOCUMENTO_ENTRADA) + folha de pagamento — NÃO pelo que foi consumido do estoque depois
     * (CONSUMO_ESTOQUE), que é excluído aqui para não contar a mesma mercadoria duas vezes
     * (uma na compra, outra no consumo). O custo do que foi consumido continua disponível como
     * informação em Lotes/Animais (ver SLote/SAnimal), só não soma de novo no DRE.
     */
    @Transactional
    public ResumoMensalDto gerarResumoMensal(int ano, int mes, String emailUsuario) {
        resolveUsuarioModulo(emailUsuario);

        BigDecimal totalEntradas = lancamentoInterface.somarPorBloco(ano, mes, EnTipoMovimentoFinanceiro.ENTRADA, null);
        BigDecimal totalSaidasCusto = lancamentoInterface.somarPorBlocoExcluindoOrigem(
                ano, mes, EnTipoMovimentoFinanceiro.SAIDA, EnNaturezaFinanceira.CUSTO,
                EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE);
        BigDecimal totalSaidasDespesa = lancamentoInterface.somarPorBlocoExcluindoOrigem(
                ano, mes, EnTipoMovimentoFinanceiro.SAIDA, EnNaturezaFinanceira.GASTO,
                EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE);
        BigDecimal totalSaidas = totalSaidasCusto.add(totalSaidasDespesa);
        BigDecimal lucroLiquido = totalEntradas.subtract(totalSaidas);

        return new ResumoMensalDto(ano, mes, totalEntradas, totalSaidasCusto, totalSaidasDespesa,
                totalSaidas, lucroLiquido);
    }

    /**
     * Mesmo cálculo de gerarResumoMensal, sem revalidar o perfil — para uso interno de SResumo,
     * que já decidiu (antes de chamar aqui) se o perfil do usuário pode ver dados financeiros.
     */
    @Transactional
    ResumoMensalDto gerarResumoMensalInterno(int ano, int mes) {
        BigDecimal totalEntradas = lancamentoInterface.somarPorBloco(ano, mes, EnTipoMovimentoFinanceiro.ENTRADA, null);
        BigDecimal totalSaidasCusto = lancamentoInterface.somarPorBlocoExcluindoOrigem(
                ano, mes, EnTipoMovimentoFinanceiro.SAIDA, EnNaturezaFinanceira.CUSTO,
                EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE);
        BigDecimal totalSaidasDespesa = lancamentoInterface.somarPorBlocoExcluindoOrigem(
                ano, mes, EnTipoMovimentoFinanceiro.SAIDA, EnNaturezaFinanceira.GASTO,
                EnOrigemLancamentoFinanceiro.CONSUMO_ESTOQUE);
        BigDecimal totalSaidas = totalSaidasCusto.add(totalSaidasDespesa);
        BigDecimal lucroLiquido = totalEntradas.subtract(totalSaidas);

        return new ResumoMensalDto(ano, mes, totalEntradas, totalSaidasCusto, totalSaidasDespesa,
                totalSaidas, lucroLiquido);
    }

    /**
     * Receita do mês agrupada por origem: um bucket "Leite" (soma de EDocumentoSaida.valorTotal
     * das vendas de leite) e um bucket por raça (soma de EVendaAnimalItem.valorVenda) para vendas
     * de animal. Retorna a maior e a menor fonte — usado pelo card "Resumo".
     */
    @Transactional
    FontesReceitaDto analisarFontesReceita(int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<EDocumentoSaida> vendas = documentoSaidaInterface.findByStatusAndDataEmissaoBetween(
                EnStatus.A, inicio, fim);

        Map<String, BigDecimal> porFonte = new LinkedHashMap<>();
        for (EDocumentoSaida venda : vendas) {
            if (venda.getTipoDocumento() == EnTipoDocumentoSaida.VENDA_LEITE) {
                porFonte.merge("Leite", venda.getValorTotal(), BigDecimal::add);
            } else {
                for (EVendaAnimalItem item : venda.getItensAnimal()) {
                    String raca = item.getAnimal().getRaca() != null
                            ? item.getAnimal().getRaca().getNome() : "Sem raça";
                    porFonte.merge(raca, item.getValorVenda(), BigDecimal::add);
                }
            }
        }

        if (porFonte.isEmpty()) {
            return new FontesReceitaDto(null, null, null, null);
        }

        Map.Entry<String, BigDecimal> maior = Collections.max(porFonte.entrySet(), Map.Entry.comparingByValue());
        Map.Entry<String, BigDecimal> menor = Collections.min(porFonte.entrySet(), Map.Entry.comparingByValue());

        return new FontesReceitaDto(maior.getKey(), maior.getValue(), menor.getKey(), menor.getValue());
    }

    @Transactional
    public List<LancamentoFinanceiroRespostaDto> listarPorBloco(int ano, int mes, String emailUsuario) {
        resolveUsuarioModulo(emailUsuario);
        return lancamentoInterface.findByAnoCompetenciaAndMesCompetenciaOrderByDataCompetenciaAsc(ano, mes)
                .stream().map(this::toRespostaDto).collect(Collectors.toList());
    }

    private LancamentoFinanceiroRespostaDto toRespostaDto(ELancamentoFinanceiro lancamento) {
        LancamentoFinanceiroRespostaDto dto = new LancamentoFinanceiroRespostaDto();
        dto.setId(lancamento.getId());
        dto.setTipoMovimento(lancamento.getTipoMovimento());
        dto.setNaturezaFinanceira(lancamento.getNaturezaFinanceira());
        dto.setOrigem(lancamento.getOrigem());
        dto.setOrigemId(lancamento.getOrigemId());
        dto.setDescricao(lancamento.getDescricao());
        dto.setValor(lancamento.getValor());
        dto.setDataCompetencia(lancamento.getDataCompetencia());
        dto.setVirtual(lancamento.getVirtual());
        dto.setCriadoPorEmail(lancamento.getCriadoPorEmail());
        return dto;
    }
}
