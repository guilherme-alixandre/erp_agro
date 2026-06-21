package br.com.gado.application.dto.loteDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferenciaAnimalDto {

    @NotNull(message = "O ID do animal é obrigatório.")
    private Long animalId;

    @NotNull(message = "O ID do lote de destino é obrigatório.")
    private Long loteDestinoId;

    @NotNull(message = "O ID do setor de destino é obrigatório.")
    private Long setorDestinoId;
}
