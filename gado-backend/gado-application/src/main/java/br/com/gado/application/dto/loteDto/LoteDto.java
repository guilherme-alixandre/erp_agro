package br.com.gado.application.dto.loteDto;

import br.com.gado.application.dto.AbstractDTO;
import br.com.gado.domain.entities.EUsuario;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoteDto extends AbstractDTO {
    private String descricao;
    private String racaPredominante;
    private EUsuario usuario;
}
