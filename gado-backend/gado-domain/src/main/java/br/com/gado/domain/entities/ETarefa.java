package br.com.gado.domain.entities;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class ETarefa {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long Id;

   private String descricao;
   private Date dataLimite;
   private boolean status;

   @ManyToOne
   @JoinColumn(name = "lista_tarefa_id")
   private EListasTarefas listasTarefa_id;
}
