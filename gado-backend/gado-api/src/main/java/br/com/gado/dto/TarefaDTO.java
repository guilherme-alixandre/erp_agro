package br.com.gado.dto;

import br.com.gado.domain.entities.EAbstract;
import br.com.gado.domain.entities.EListasTarefas;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class TarefaDTO extends AbstractDTO{

    private String descricao;
    private Date dataLimite;
    private boolean statusConclusao;
    private EListasTarefas lista;
}
