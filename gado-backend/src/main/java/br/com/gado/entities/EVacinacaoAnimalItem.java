package br.com.gado.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "vacinacao_animal_item")
@Data
public class EVacinacaoAnimalItem extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "vacinacao_id", nullable = false)
    @JsonIgnore
    private EVacinacaoAnimal vacinacao;

    @ManyToOne
    @JoinColumn(name = "animal_id", nullable = false)
    private EAnimal animal;
}
