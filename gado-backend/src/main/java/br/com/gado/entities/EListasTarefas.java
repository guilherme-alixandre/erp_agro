package br.com.gado.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "listas_tarefas")
@Data
public class EListasTarefas extends EAbstract{

    private String nomeLista;
}
