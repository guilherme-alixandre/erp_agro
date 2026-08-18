package br.com.gado.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "consumo_insumo")
@Data
public class EConsumoInsumo extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    @JsonIgnore
    private EInsumo insumo;

    @ManyToOne
    @JoinColumn(name = "setor_id", nullable = false)
    @JsonIgnore
    private ESetor setor;

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
     * histórico não mude caso o fator de conversão do insumo seja alterado no futuro.
     */
    @Column(name = "quantidade_baixa_unidade_primaria", nullable = false)
    private Double quantidadeBaixaUnidadePrimaria;

    /** Fotografia do total de animais do setor no momento do registro (para relatórios futuros). */
    @Column(name = "total_animais_setor", nullable = false)
    private Integer totalAnimaisSetor;

    /** quantidadeRegistrada / totalAnimaisSetor. Nulo quando o setor não tinha animais alocados. */
    @Column(name = "consumo_por_animal")
    private Double consumoPorAnimal;

    @Column(name = "data_consumo", nullable = false)
    private LocalDateTime dataConsumo;

    /** E-mail de quem registrou (qualquer perfil). Guardado como texto, como em EMedicaoMeta. */
    @Column(name = "registrado_por_email")
    private String registradoPorEmail;
}
