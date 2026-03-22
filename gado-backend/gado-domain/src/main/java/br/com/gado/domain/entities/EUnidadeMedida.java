package br.com.gado.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class EUnidadeMedida extends EAbstract{

    private String unidade;
}
