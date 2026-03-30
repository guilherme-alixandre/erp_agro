package br.com.gado.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tarefa")
@Data
public class ETarefa extends EAbstract{

   private String descricao;
   private Date dataLimite;
   private boolean statusConclusao;

   @ManyToOne
   @JoinColumn(name = "lista_tarefa_id")
   private EListasTarefas listasTarefaId;
}
