package br.com.gado.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "unidade_medida")
@Data
public class EUnidadeMedida extends EAbstract{

    private String unidade;
}
