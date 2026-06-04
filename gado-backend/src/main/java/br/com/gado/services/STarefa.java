package br.com.gado.services;

import br.com.gado.dto.TarefaDTO;
import br.com.gado.entities.EListasTarefas;
import br.com.gado.entities.ETarefa;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IListasTarefas;
import br.com.gado.repositories.ITarefa;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class STarefa {

    private static final Logger log = LoggerFactory.getLogger(STarefa.class);

    @Autowired
    private ITarefa tarefaInterface;

    @Autowired
    private IListasTarefas listaTarefas;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public TarefaDTO criarTarefa(TarefaDTO novaTarefa, Long listaId) {
        try {
            EListasTarefas lista = listaTarefas.findById(listaId)
                    .orElseThrow(() -> {
                        log.error("Erro ao criar tarefa: Lista ID {} não encontrada.", listaId);
                        return new RuntimeException("Lista de tarefas não encontrada");
                    });

            ETarefa tarefa = modelMapper.map(novaTarefa, ETarefa.class);
            tarefa.setStatusConclusao(false);
            tarefa.setListasTarefaId(lista);

            ETarefa tarefaSalva = tarefaInterface.save(tarefa);
            log.info("Tarefa criada com sucesso! ID: {}", tarefaSalva.getId());

            return modelMapper.map(tarefaSalva, TarefaDTO.class);
        } catch (Exception e) {
            log.error("Erro ao salvar tarefa: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public TarefaDTO buscarTarefaPorId(Long tarefaId) {
        ETarefa tarefaEntity = this.tarefaInterface
                .findById(tarefaId)
                .orElseThrow(EntityNotFoundException::new);
        return this.modelMapper.map(tarefaEntity, TarefaDTO.class);
    }

    @Transactional
    public TarefaDTO atualizarTarefa(TarefaDTO tarefaParaAtualizar, Long tarefaId) {
        ETarefa existingEntity = this.tarefaInterface
                .findById(tarefaId)
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        this.modelMapper.map(tarefaParaAtualizar, existingEntity);

        try {
            ETarefa tarefaSalva = this.tarefaInterface.save(existingEntity);
            return modelMapper.map(tarefaSalva, TarefaDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar tarefa: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public String excluirTarefa(Long tarefaId) {
        ETarefa tarefaParaExcluir = this.tarefaInterface
                .findById(tarefaId)
                .orElseThrow(EntityNotFoundException::new);

        tarefaParaExcluir.setStatus(EnStatus.I);

        try {
            this.tarefaInterface.save(tarefaParaExcluir);
            return "tarefa excluída com sucesso";
        } catch (Exception e) {
            log.error("Erro ao excluir tarefa: {}", e.getMessage(), e);
            return "erro ao excluir tarefa";
        }
    }
}
