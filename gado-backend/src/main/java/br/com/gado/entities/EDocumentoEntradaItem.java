package br.com.gado.entities;

import br.com.gado.enums.EnNaturezaFinanceira;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Item de um EDocumentoEntrada. descricaoXml/codigoXml preservam exatamente o que veio do
 * XML/recibo (nunca editáveis via editarNfe). produto é o vínculo com o catálogo (EInsumo),
 * preenchido pela ação "Vincular" — permitida ao perfil Financeiro sem exigir senha, pois não
 * altera nenhum valor do documento (ver SDocumentoEntrada.vincularProduto).
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "documento_entrada_item")
@Data
public class EDocumentoEntradaItem extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "documento_entrada_id", nullable = false)
    @JsonIgnore
    private EDocumentoEntrada documentoEntrada;

    /** Descrição exatamente como veio do XML/recibo — nunca editável, preserva o documento original. */
    @Column(name = "descricao_xml", nullable = false)
    private String descricaoXml;

    @Column(name = "codigo_xml", length = 60)
    private String codigoXml;

    /** Produto do catálogo (EInsumo) ao qual este item foi vinculado. Nulo até a vinculação. */
    @ManyToOne
    @JoinColumn(name = "produto_id")
    private EInsumo produto;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 4)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false)
    private Boolean vinculado = false;

    @Column(name = "vinculado_por_email")
    private String vinculadoPorEmail;

    @Column(name = "vinculado_em")
    private LocalDateTime vinculadoEm;

    /**
     * CUSTO ou GASTO deste item. Copiado do grupoProduto do produto vinculado no momento da
     * vinculação (snapshot — sobrevive a uma futura reclassificação do grupo). Para itens de
     * RECIBO_SIMPLES sem produto de catálogo, é definido diretamente no cadastro do recibo.
     * Enquanto não vinculado e sem valor explícito, o lançamento financeiro assume GASTO por
     * padrão (ver SLancamentoFinanceiro.registrarOuAtualizarSaidaItemDocumento).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "natureza_financeira", length = 20)
    private EnNaturezaFinanceira naturezaFinanceira;
}
