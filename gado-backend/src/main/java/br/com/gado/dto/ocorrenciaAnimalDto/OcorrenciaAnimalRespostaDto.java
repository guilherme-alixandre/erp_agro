package br.com.gado.dto.ocorrenciaAnimalDto;

import br.com.gado.enums.EnTipoOcorrencia;
import lombok.Data;

import java.util.Date;

@Data
public class OcorrenciaAnimalRespostaDto {

    private Long id;
    private EnTipoOcorrencia tipoOcorrencia;
    private Date dataOcorrencia;
    private String observacao;
}
