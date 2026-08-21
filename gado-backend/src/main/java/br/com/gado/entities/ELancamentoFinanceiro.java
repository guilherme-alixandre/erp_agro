package br.com.gado.entities;

import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnOrigemLancamentoFinanceiro;
import br.com.gado.enums.EnTipoMovimentoFinanceiro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Livro-razão do módulo financeiro: todo fato que afeta o DRE (venda, custo de produção,
 * despesa administrativa) vira uma linha aqui — seja lançada manualmente ou gerada
 * automaticamente por outro módulo (Insumos, Folha, Documentos de Entrada).
 * SLancamentoFinanceiro.gerarResumoMensal() consolida esta tabela por Bloco Ano/Mês
 * (anoCompetencia/mesCompetencia).
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "lancamento_financeiro")
@Data
public class ELancamentoFinanceiro extends EAbstract {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimento", nullable = false, length = 10)
    private EnTipoMovimentoFinanceiro tipoMovimento;

    /** Aplica-se apenas a SAIDA — uma ENTRADA (venda) não é Custo nem Despesa. */
    @Enumerated(EnumType.STRING)
    @Column(name = "natureza_financeira", length = 20)
    private EnNaturezaFinanceira naturezaFinanceira;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EnOrigemLancamentoFinanceiro origem;

    /** Id da entidade de origem (EDocumentoEntradaItem, EPagamentoFuncionario, EConsumoEstoqueItem, ...). */
    @Column(name = "origem_id")
    private Long origemId;

    @Column(nullable = false)
    private String descricao;

    /** Sempre positivo — o sinal é dado por tipoMovimento. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    /** Data do fato gerador (não da baixa/pagamento) — é o que define o Bloco Ano/Mês. */
    @Column(name = "data_competencia", nullable = false)
    private LocalDate dataCompetencia;

    @Column(name = "ano_competencia", nullable = false)
    private Integer anoCompetencia;

    @Column(name = "mes_competencia", nullable = false)
    private Integer mesCompetencia;

    /** true quando gerado automaticamente pelo sistema (ex: saída de estoque), sem lançamento manual. */
    @Column(nullable = false)
    private Boolean virtual = false;

    @Column(name = "criado_por_email", nullable = false)
    private String criadoPorEmail;
}
