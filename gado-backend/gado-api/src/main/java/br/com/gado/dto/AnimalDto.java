package br.com.gado.dto;

import br.com.gado.domain.enums.EnSexoAnimal;
import br.com.gado.domain.enums.EnStatusAnimal;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnimalDto {

    // talvez eu tenha que ter um unique = true
    private String codigoBrinco;
    private String nome;
    private String cor;
    private LocalDateTime dataNascimento;
    private Double pesoAtual;
    private String raca;
    private String tamanho;
    private EnSexoAnimal sexo;
    private EnStatusAnimal status;
    private Long usuario_id;
}
