package br.com.gado.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "consumo_estoque_item")
@Data
public class EConsumoEstoqueItem extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "consumo_estoque_id", nullable = false)
    @JsonIgnore
    private EConsumoEstoque consumoEstoque;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    @JsonIgnore
    private EInsumo insumo;

    /** Quantidade exatamente como informada pelo usuário, na unidade de registro escolhida. */
    @Column(name = "quantidade_registrada", nullable = false)
    private Double quantidadeRegistrada;

    /** Unidade em que quantidadeRegistrada foi informada (primária ou secundária do insumo). */
    @ManyToOne
    @JoinColumn(name = "unidade_registro_id", nullable = false)
    @JsonIgnore
    private EUnidadeMedida unidadeRegistro;

    /**
     * Quantidade efetivamente debitada do estoque, já convertida para a unidade
     * primária do insumo. Persistida (e não recalculada depois) para que o
     * cancelamento sempre estorne exatamente o que foi debitado.
     */
    @Column(name = "quantidade_baixa_unidade_primaria", nullable = false)
    private Double quantidadeBaixaUnidadePrimaria;
}
