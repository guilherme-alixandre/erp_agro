package br.com.gado.dto.documentoEntradaDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecusaDocumentoDto {

    @NotBlank(message = "Justifique o motivo da recusa.")
    private String justificativa;
}
