package br.com.gado.dto.folhaPagamentoDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EstornoPagamentoDto {

    @NotBlank(message = "Informe o motivo do estorno.")
    private String motivoEstorno;
}
