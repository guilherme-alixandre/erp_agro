package br.com.gado.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade de junção entre ELote e ESetor.
 *
 * <p>Representa a alocação física de um lote em um setor específico.
 * Contém a lista de animais que foram distribuídos para este setor
 * dentro do lote.</p>
 *
 * <p>Exemplo: Lote LOT003 alocado em Setor A (30 animais) e Setor B (20 animais)
 * gera duas instâncias de ELoteSetor, cada uma com sua respectiva lista de EAnimal.</p>
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "lote_setor",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_lote_setor",
        columnNames = {"lote_id", "setor_id"}
    )
)
@Data
public class ELoteSetor extends EAbstract {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ELote lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ESetor setor;

    /**
     * Animais deste lote que foram alocados especificamente neste setor.
     * A soma de animais em todos os ELoteSetor de um lote = total de animais do lote.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "lote_setor_animal",
        joinColumns = @JoinColumn(name = "lote_setor_id"),
        inverseJoinColumns = @JoinColumn(name = "animal_id")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<EAnimal> animais = new ArrayList<>();
}
