package br.com.gado.dto.vacinacaoAnimalDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VacinacaoAnimalCancelamentoDto {

    @NotBlank(message = "Informe a justificativa do cancelamento.")
    private String motivoCancelamento;
}
