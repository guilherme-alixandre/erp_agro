package br.com.gado.application.dto;

import br.com.gado.application.dto.AbstractDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnidadeMedidaDTO extends AbstractDTO {
    private String unidade;
}
