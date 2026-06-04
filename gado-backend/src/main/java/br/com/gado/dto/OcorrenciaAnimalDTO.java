package br.com.gado.dto;

import br.com.gado.entities.EAnimal;
import br.com.gado.enums.EnTipoOcorrencia;
import lombok.Data;

import java.util.Date;

@Data
public class OcorrenciaAnimalDTO extends AbstractDTO {

    private EnTipoOcorrencia tipoOcorrencia;
    private Date dataOcorrencia;
    private String observacao;
    private EAnimal idAnimal;

}
