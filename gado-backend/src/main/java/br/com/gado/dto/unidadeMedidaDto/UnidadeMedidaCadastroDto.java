package br.com.gado.dto.unidadeMedidaDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnidadeMedidaCadastroDto {

    @NotBlank(message = "A unidade é obrigatória.")
    private String unidade;
}
