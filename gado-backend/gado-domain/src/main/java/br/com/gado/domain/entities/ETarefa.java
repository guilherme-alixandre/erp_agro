package br.com.gado.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class ETarefa extends EAbstract{

   private String descricao;
   private Date dataLimite;
   private boolean statusConclusao;

   @ManyToOne
   @JoinColumn(name = "lista_tarefa_id")
   private EListasTarefas listasTarefaId;
}
