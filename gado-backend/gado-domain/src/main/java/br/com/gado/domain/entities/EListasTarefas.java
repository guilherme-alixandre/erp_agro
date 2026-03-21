package br.com.gado.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class EListasTarefas extends EAbstract{

    private String nomeLista;
}
