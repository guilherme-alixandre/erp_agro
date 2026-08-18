package br.com.gado.entities;

import br.com.gado.enums.EnTipoInsumo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "insumo")
@Data
public class EInsumo extends EAbstract{

    private String nome;
    private Double estoqueMinimo;

    /** Saldo em estoque, sempre expresso na unidade de medida primária. */
    private Double saldoAtual;

    @ManyToOne
    @JoinColumn(name = "parceiro_id")
    private EParceiro parceiro;

    @Enumerated(EnumType.STRING)
    private EnTipoInsumo tipo;

    private Boolean pendente;

    // ── Catálogo de Produtos ─────────────────────────────────────────────

    /** Grupo ao qual o produto pertence — define o prefixo do codigoProduto gerado. */
    @ManyToOne
    @JoinColumn(name = "grupo_produto_id")
    private EGrupoProduto grupoProduto;

    /**
     * Código do catálogo: [2 dígitos do grupo] + [6 dígitos sequenciais], ex: "01000001".
     * Gerado automaticamente por SInsumo a partir do grupoProduto — nunca editável depois de criado.
     */
    @Column(name = "codigo_produto", unique = true, length = 8)
    private String codigoProduto;

    // ── Unidades de medida e conversão ──────────────────────────────────

    /** Unidade em que o saldo/estoque é controlado (ex: SACA). */
    @ManyToOne
    @JoinColumn(name = "unidade_medida_primaria_id")
    private EUnidadeMedida unidadeMedidaPrimaria;

    /** Unidade usada no consumo diário (ex: KG). Opcional. */
    @ManyToOne
    @JoinColumn(name = "unidade_medida_secundaria_id")
    private EUnidadeMedida unidadeMedidaSecundaria;

    /**
     * Quantas unidades secundárias equivalem a 1 unidade primária
     * (ex: fatorConversao = 40 significa 1 SACA = 40 KG).
     * Obrigatório sempre que unidadeMedidaSecundaria estiver preenchida.
     */
    private Double fatorConversao;

    // ── Financeiro ───────────────────────────────────────────────────────

    /** Preço médio ponderado por unidade primária, recalculado a cada entrada manual de estoque. */
    private Double precoCompraMedio;

    /** Preço unitário (unidade primária) pago na última compra registrada. */
    private Double precoUltimaCompra;

    // ── Dados da Nota Fiscal (base para futura importação de XML) ──────

    private String numeroNf;

    private String chaveAcessoNf;

}
