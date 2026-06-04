package br.com.gado.entities;

import br.com.gado.enums.EnTipoOcorrencia;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ocorrencia_animal")
@Data
public class EOcorrenciaAnimal extends EAbstract{

    @Enumerated(EnumType.STRING)
    private EnTipoOcorrencia tipoOcorrencia;

    private Date dataOcorrencia;
    private String observacao;

    @ManyToOne
    @JoinColumn(name = "id_animal_id")
    private EAnimal idAnimal;

}
