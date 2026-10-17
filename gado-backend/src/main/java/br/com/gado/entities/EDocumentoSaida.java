package br.com.gado.entities;

import br.com.gado.enums.EnFormaPagamento;
import br.com.gado.enums.EnTipoDocumentoSaida;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Documento de saída que gera receita para a fazenda: venda de leite ou venda/abate de animais.
 * Espelha EDocumentoEntrada (mesmo padrão de nomenclatura), mas do lado da receita — juntos, os
 * dois formam a listagem unificada de "Notas Fiscais" (ver SNotaFiscal).
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "documento_saida")
@Data
public class EDocumentoSaida extends EAbstract {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private EnTipoDocumentoSaida tipoDocumento;

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    @Column(name = "chave_acesso", length = 44)
    private String chaveAcesso;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", length = 15)
    private EnFormaPagamento formaPagamento;

    /** Vencimento único (se à vista) ou da primeira parcela — as demais vencem +1 mês cada. */
    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "numero_parcelas")
    private Integer numeroParcelas;

    /** Preenchido apenas quando formaPagamento = PARCELADO. */
    @Column(name = "valor_parcela", precision = 15, scale = 2)
    private BigDecimal valorParcela;

    @Column(name = "criado_por_email", nullable = false)
    private String criadoPorEmail;

    /** Parceiro (tipo COMPRADOR ou AMBOS) que comprou a mercadoria desta venda. */
    @ManyToOne
    @JoinColumn(name = "comprador_id")
    private EParceiro comprador;

    @OneToMany(mappedBy = "documentoSaida", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EVendaLeiteItem> itensLeite = new ArrayList<>();

    @OneToMany(mappedBy = "documentoSaida", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EVendaAnimalItem> itensAnimal = new ArrayList<>();
}
