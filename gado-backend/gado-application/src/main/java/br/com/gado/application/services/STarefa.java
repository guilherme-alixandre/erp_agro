package br.com.gado.application.services;

import br.com.gado.domain.entities.EListasTarefas;
import br.com.gado.domain.entities.ETarefa;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.dto.TarefaDTO;
import br.com.gado.infrastructure.persistence.repositories.IListasTarefas;
import br.com.gado.infrastructure.persistence.repositories.ITarefa;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

@Service
public class STarefa {
    private static final Logger log = LoggerFactory.getLogger(STarefa.class);
    private final ITarefa tarefaInterface;
    private final IListasTarefas listaTarefas;
    private final ModelMapper modelMapper;
    private final TransactionalOperator transactionalOperator;

    public STarefa (ITarefa tarefaInterface, IListasTarefas listaTarefasInterface, TransactionalOperator transactionalOperator) {
        this.tarefaInterface = tarefaInterface;
        this.listaTarefas = listaTarefasInterface;
        this.modelMapper = new ModelMapper();
        this.transactionalOperator = transactionalOperator;
    }

    @Transactional
    public TarefaDTO criarTarefa(TarefaDTO novaTarefa, Long listaId) {
        try{
            EListasTarefas lista = (EListasTarefas) listaTarefas.findByListaTerafaId(listaId)
                    .orElseThrow(() -> {
                        log.error("Erro ao criar tarefa: Lista ID {} não encontrada.", listaId);
                        return new RuntimeException("Lista de tarefas não encontrada");
                    });

            ETarefa tarefa = modelMapper.map(novaTarefa, ETarefa.class);

            tarefa.setDescricao(novaTarefa.getDescricao());
            tarefa.setDataLimite(novaTarefa.getDataLimite());
            tarefa.setStatusConclusao(false);
            tarefa.setListasTarefaId(lista);

            ETarefa tarefaSalva = tarefaInterface.save(tarefa);
            log.info("Tarefa criada com sucesso! ID: {}", tarefaSalva.getId());

            return modelMapper.map(tarefaSalva, TarefaDTO.class);

        }catch (Exception e){
            log.error("Erro ao salvar tarefa: {}", e.getMessage(), e);
            throw e; // Lança a exceção para que o @Transactional faça o rollback
        }
    }

    @Transactional
    public TarefaDTO buscarTarefaPorId(Long tarefaId) {
        ETarefa tarefaEntity = this.tarefaInterface
                .findByTarefaId(tarefaId)
                .orElseThrow(EntityNotFoundException::new);
        return this.modelMapper.map(tarefaEntity, TarefaDTO.class);
    }

    @Transactional
    public TarefaDTO atualizarTarefa(TarefaDTO tarefaParaAtualizar, Long tarefaId) {
        // 1. Busca a entidade existente (para garantir que os dados atuais não se percam)
        ETarefa existingEntity = this.tarefaInterface
                .findByTarefaId(tarefaId)
                .orElseThrow(EntityNotFoundException::new);

        // 2. Configura o ModelMapper para ignorar campos nulos vindo do DTO
        this.modelMapper.getConfiguration().setSkipNullEnabled(true);

        // 3. Mapeia os dados do DTO para a entidade existente (apenas campos não nulos)
        this.modelMapper.map(tarefaParaAtualizar, existingEntity);

        // 4. Salva a entidade atualizada no banco (Passo essencial que faltava!)
        try{
            ETarefa tarefaSalva = this.tarefaInterface.save(existingEntity);

            // 5. Retorna o DTO atualizado
            return modelMapper.map(tarefaSalva, TarefaDTO.class);
        } catch (Exception e){
            log.error("Erro ao atualizar tarefa: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public Boolean excluirTarefa(Long tarefaId) {

        ETarefa tarefaParaExcluir = this.tarefaInterface
                .findByTarefaId(tarefaId)
                .orElseThrow(EntityNotFoundException::new);

        tarefaParaExcluir.setStatus(EnStatus.I);
        try{
            this.tarefaInterface.save(tarefaParaExcluir);
            return true;
        } catch (Exception e){
            log.error("Erro ao excluir tarefa: {}", e.getMessage(), e);
        }

        return false;
    }
}
