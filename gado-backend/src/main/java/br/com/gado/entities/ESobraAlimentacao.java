package br.com.gado.entities;

import br.com.gado.enums.EnStatusSobra;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Sobra de uma alimentação já registrada em "Alimentar Setores" (EConsumoInsumo): quanto sobrou
 * antes da próxima alimentação, se foi reaproveitada, e o resultado da avaliação (dentro da faixa
 * ideal, recomendação de ajuste, ou perda). Uma sobra por alimentação (FK única).
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sobra_alimentacao")
@Data
public class ESobraAlimentacao extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "consumo_insumo_id", nullable = false, unique = true)
    @JsonIgnore
    private EConsumoInsumo consumoInsumo;

    /** Quantidade de sobra exatamente como informada pelo usuário, na unidade de registro escolhida. */
    @Column(name = "quantidade_sobra_registrada", nullable = false)
    private Double quantidadeSobraRegistrada;

    /** Unidade em que quantidadeSobraRegistrada foi informada (primária ou secundária do insumo). */
    @ManyToOne
    @JoinColumn(name = "unidade_registro_id", nullable = false)
    @JsonIgnore
    private EUnidadeMedida unidadeRegistro;

    /** Quantidade de sobra convertida para a unidade primária do insumo — base de todos os cálculos. */
    @Column(name = "quantidade_sobra_unidade_primaria", nullable = false)
    private Double quantidadeSobraUnidadePrimaria;

    /** quantidadeSobraUnidadePrimaria / quantidadeBaixaUnidadePrimaria da alimentação, em %. */
    @Column(name = "percentual_sobra", nullable = false)
    private Double percentualSobra;

    @Column(nullable = false)
    private Boolean reaproveitado;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_faixa", nullable = false, length = 10)
    private EnStatusSobra statusFaixa;

    /** Quantidade recomendada de ajuste (aumentar se ABAIXO, reduzir se ACIMA), na unidade primária. */
    @Column(name = "quantidade_ajuste_recomendada")
    private Double quantidadeAjusteRecomendada;

    /** Mensagem pronta (pt-BR) explicando o resultado — reaproveitada para a listagem de "Alimentar Setores". */
    @Column(length = 500)
    private String mensagem;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    @Column(name = "registrado_por_email")
    private String registradoPorEmail;
}
