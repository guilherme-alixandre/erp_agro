package br.com.gado.dto.consumoEstoqueDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConsumoEstoqueCancelamentoDto {

    @NotBlank(message = "Informe a justificativa do cancelamento.")
    private String motivoCancelamento;
}
