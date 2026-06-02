package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnSexoAnimal;
import br.com.gado.domain.enums.EnStatusAnimal;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    @ManyToOne
    @JoinColumn(name = "pessoa_id")
    private EUsuario usuario;
}
