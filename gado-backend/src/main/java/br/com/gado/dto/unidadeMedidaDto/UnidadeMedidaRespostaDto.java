package br.com.gado.dto.unidadeMedidaDto;

import br.com.gado.enums.EnStatus;
import lombok.Data;

@Data
public class UnidadeMedidaRespostaDto {
    private Long id;
    private String unidade;
    private EnStatus status;
}
