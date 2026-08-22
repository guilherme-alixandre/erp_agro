package br.com.gado.dto;

import br.com.gado.enums.EnSexoAnimal;
import br.com.gado.enums.EnStatusAnimal;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AnimalDto extends AbstractDTO {

    // talvez eu tenha que ter um unique = true
    private String codigoBrinco;
    private String nome;
    private String cor;
    private LocalDateTime dataNascimento;

    @DecimalMin(value = "30.0", message = "O peso mínimo para cadastro é 30kg.")
    private Double pesoAtual;
    private String raca;
    private Double alturaCernelha;
    private Double perimetroToracico;
    private Double comprimentoCorporal;
    private EnSexoAnimal sexo;
    private EnStatusAnimal statusAnimal;
    private List<VacinacaoDTO> vacinas;
}
