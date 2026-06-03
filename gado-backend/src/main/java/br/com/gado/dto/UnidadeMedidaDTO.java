package br.com.gado.dto;

import br.com.gado.dto.AbstractDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnidadeMedidaDTO extends AbstractDTO {
    private String unidade;
}
