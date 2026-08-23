package br.com.gado.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "vacinacao_animal")
@Data
public class EVacinacaoAnimal extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    private EInsumo insumo;

    /** Dose aplicada por animal, na unidade de registro escolhida. */
    @Column(name = "quantidade_por_animal", nullable = false)
    private Double quantidadePorAnimal;

    @ManyToOne
    @JoinColumn(name = "unidade_registro_id", nullable = false)
    private EUnidadeMedida unidadeRegistro;

    /** Dose por animal já convertida para a unidade primária do insumo. */
    @Column(name = "quantidade_baixa_por_animal_unidade_primaria", nullable = false)
    private Double quantidadeBaixaPorAnimalUnidadePrimaria;

    /**
     * Total debitado do estoque (dose por animal x nº de animais), persistido
     * para que edição/cancelamento sempre estornem exatamente o que foi debitado.
     */
    @Column(name = "quantidade_total_baixa_unidade_primaria", nullable = false)
    private Double quantidadeTotalBaixaUnidadePrimaria;

    /** Preenchido apenas quando a aplicação teve origem em "lote inteiro" — informativo. */
    @ManyToOne
    @JoinColumn(name = "lote_id")
    private ELote lote;

    @Column(name = "data_aplicacao", nullable = false)
    private LocalDateTime dataAplicacao;

    @Column(name = "criado_por_email", nullable = false)
    private String criadoPorEmail;

    @Column(nullable = false)
    private Boolean cancelado = false;

    @Column(name = "motivo_cancelamento")
    private String motivoCancelamento;

    @Column(name = "cancelado_por_email")
    private String canceladoPorEmail;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @OneToMany(mappedBy = "vacinacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EVacinacaoAnimalItem> itens = new ArrayList<>();
}
