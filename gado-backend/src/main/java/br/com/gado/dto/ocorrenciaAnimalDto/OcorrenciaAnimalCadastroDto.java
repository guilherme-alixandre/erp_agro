package br.com.gado.dto.ocorrenciaAnimalDto;

import br.com.gado.enums.EnTipoOcorrencia;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class OcorrenciaAnimalCadastroDto {

    @NotNull(message = "Informe o animal.")
    private Long animalId;

    @NotNull(message = "Informe o tipo de ocorrência.")
    private EnTipoOcorrencia tipoOcorrencia;

    @NotNull(message = "Informe a data da ocorrência.")
    private Date dataOcorrencia;

    private String observacao;
}
