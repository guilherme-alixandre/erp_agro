package br.com.gado.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "lote_setor",
        uniqueConstraints = @UniqueConstraint(name = "uq_lote_setor_alocacao", columnNames = {"lote_id", "setor_id"})
)
@Data
public class ELoteSetor extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "lote_id", nullable = false)
    @JsonIgnore
    private ELote lote;

    @ManyToOne
    @JoinColumn(name = "setor_id", nullable = false)
    private ESetor setor;

    @ManyToMany
    @JoinTable(
            name = "lote_setor_animal",
            joinColumns = @JoinColumn(name = "lote_setor_id"),
            inverseJoinColumns = @JoinColumn(name = "animal_id")
    )
    private List<EAnimal> animais = new ArrayList<>();
}
