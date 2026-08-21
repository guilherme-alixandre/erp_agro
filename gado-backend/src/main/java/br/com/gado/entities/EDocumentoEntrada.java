package br.com.gado.entities;

import br.com.gado.enums.EnStatusAprovacaoFinanceira;
import br.com.gado.enums.EnTipoDocumentoFinanceiro;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Documento de entrada (fiscal ou não) que dá origem a uma saída financeira: NF-e importada
 * via XML ou recibo simples lançado manualmente. NF-e nasce APROVADO (documento fiscal já é
 * confiável); RECIBO_SIMPLES nasce sempre PENDENTE_APROVACAO — ver SDocumentoEntrada.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "documento_entrada")
@Data
public class EDocumentoEntrada extends EAbstract {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private EnTipoDocumentoFinanceiro tipoDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_aprovacao", nullable = false, length = 25)
    private EnStatusAprovacaoFinanceira statusAprovacao;

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    @Column(length = 10)
    private String serie;

    /** Chave de acesso de 44 dígitos — preenchida apenas quando tipoDocumento = NF_E. */
    @Column(name = "chave_acesso_nfe", unique = true, length = 44)
    private String chaveAcessoNfe;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_entrada", nullable = false)
    private LocalDate dataEntrada;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private EParceiro fornecedor;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    /** Conteúdo bruto do XML importado, guardado para auditoria/reprocessamento. Nulo para RECIBO_SIMPLES. */
    @Column(name = "xml_original", columnDefinition = "TEXT")
    private String xmlOriginal;

    @Column(name = "justificativa_recusa")
    private String justificativaRecusa;

    @Column(name = "criado_por_email", nullable = false)
    private String criadoPorEmail;

    @Column(name = "aprovado_por_email")
    private String aprovadoPorEmail;

    @Column(name = "aprovado_em")
    private LocalDateTime aprovadoEm;

    /** Preenchidos somente quando editarNfe (com senha de confirmação) altera o documento. */
    @Column(name = "ultima_edicao_por_email")
    private String ultimaEdicaoPorEmail;

    @Column(name = "ultima_edicao_em")
    private LocalDateTime ultimaEdicaoEm;

    @OneToMany(mappedBy = "documentoEntrada", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EDocumentoEntradaItem> itens = new ArrayList<>();
}
