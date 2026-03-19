package br.com.gado.application.services;

import br.com.gado.domain.entities.ETarefa;
import br.com.gado.infrastructure.persistence.repositories.IListasTarefas;
import br.com.gado.infrastructure.persistence.repositories.ITarefa;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class STarefa {
    private ITarefa tarefaInterface;
    private IListasTarefas listaTarefas;

    public STarefa (ITarefa tarefaInterface, IListasTarefas listaTarefasInterface) {
        this.tarefaInterface = tarefaInterface;
        this.listaTarefas = listaTarefasInterface;
    }

    public Map<String, Object> buscarTarefaPorId(Long tarefaId) {
        Map<String, Object> response = new HashMap<>();
        Optional<ETarefa> tarefa =  tarefaInterface.findByTarefaId(tarefaId);

        if (tarefa.isPresent()) {
            response.put("mensagem", tarefa.get());
        } else {
            response.put("Tarefa não encontrada", tarefaId);
        }

        return response;
    }
}
