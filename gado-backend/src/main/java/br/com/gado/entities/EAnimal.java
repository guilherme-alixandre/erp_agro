package br.com.gado.entities;

import br.com.gado.enums.EnSexoAnimal;
import br.com.gado.enums.EnStatusAnimal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "animal")
@Data
public class EAnimal extends EAbstract{

    private String codigoBrinco;
    private String nome;
    private LocalDateTime dataNascimento;
    private Double pesoAtual;
    private String raca;
    private String cor;
    private Double alturaCernelha;
    private Double perimetroToracico;
    private Double comprimentoCorporal;

    @Enumerated(EnumType.STRING)
    private EnSexoAnimal sexo;

    @Enumerated(EnumType.STRING)
    private EnStatusAnimal statusAnimal;

    // no banco vai ficar o "pessoa_id"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EUsuario usuario;
}
