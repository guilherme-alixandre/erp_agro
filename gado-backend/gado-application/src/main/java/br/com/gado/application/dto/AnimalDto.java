package br.com.gado.application.dto;

import br.com.gado.domain.enums.EnSexoAnimal;
import br.com.gado.domain.enums.EnStatusAnimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class AnimalDto extends AbstractDTO {

    // talvez eu tenha que ter um unique = true
    private String codigoBrinco;
    private String nome;
    private String cor;
    private LocalDateTime dataNascimento;
    private Double pesoAtual;
    private String raca;
    private Double alturaCernelha;
    private Double perimetroToracico;
    private Double comprimentoCorporal;
    private EnSexoAnimal sexo;
    private EnStatusAnimal statusAnimal;
}
