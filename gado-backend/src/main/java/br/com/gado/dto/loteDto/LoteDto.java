package br.com.gado.dto.loteDto;

import br.com.gado.dto.AbstractDTO;
import br.com.gado.entities.EUsuario;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoteDto extends AbstractDTO {
    private String descricao;
    private String racaPredominante;
    private EUsuario usuario;
}
