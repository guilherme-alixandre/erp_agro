package br.com.gado.dto;

import br.com.gado.entities.EListasTarefas;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class TarefaDTO extends AbstractDTO{

    private String descricao;
    private Date dataLimite;
    private boolean statusConclusao;
    private EListasTarefas lista;
}
