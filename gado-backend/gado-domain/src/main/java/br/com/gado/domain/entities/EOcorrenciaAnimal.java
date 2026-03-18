package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnTipoOcorrencia;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class EOcorrenciaAnimal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EnTipoOcorrencia tipoOcorrencia;

    private Date dataOcorrencia;
    private String observacao;

    @ManyToOne
    @JoinColumn(name = "id_animal_id")
    private EAnimal idAnimal;

}
