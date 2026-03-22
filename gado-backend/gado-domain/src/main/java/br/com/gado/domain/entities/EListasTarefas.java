package br.com.gado.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class EListasTarefas extends EAbstract{

    private String nomeLista;
}
