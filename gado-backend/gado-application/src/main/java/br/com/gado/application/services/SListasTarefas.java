package br.com.gado.application.services;

import br.com.gado.application.dto.ListasTarefasDTO;
import br.com.gado.domain.entities.EListasTarefas;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IListasTarefas;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SListasTarefas {

    private static final Logger log = LoggerFactory.getLogger(SListasTarefas.class);
    private final IListasTarefas listasTarefasInterface;
    private final ModelMapper modelMapper;

    @Autowired
    public SListasTarefas(IListasTarefas listasTarefas, ModelMapper modelMapper) {
        this.listasTarefasInterface = listasTarefas;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public ListasTarefasDTO criarListaDeTarefas(ListasTarefasDTO dadosLista) {
        EListasTarefas listaTarefas = modelMapper.map(dadosLista, EListasTarefas.class);

        EListasTarefas listaSalva = listasTarefasInterface.save(listaTarefas);
        return modelMapper.map(listaSalva, ListasTarefasDTO.class);
    }

    @Transactional
    public ListasTarefasDTO buscarListaDeTarefasPorId(Long listaId) {
        EListasTarefas listaTarefaEntity = this.listasTarefasInterface
                .findById(listaId)
                .orElseThrow(EntityNotFoundException::new);
        return modelMapper.map(listaTarefaEntity, ListasTarefasDTO.class);
    }

    @Transactional
    public ListasTarefasDTO atualizarListaDeTarefasPorId(ListasTarefasDTO listaParaAtualizar, Long listaId) {
        EListasTarefas existingEntity = this.listasTarefasInterface
                .findById(listaId)
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        this.modelMapper.map(listaParaAtualizar, existingEntity);

        try {
            EListasTarefas listaSalva = this.listasTarefasInterface.save(existingEntity);
            return modelMapper.map(listaSalva, ListasTarefasDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar tarefa: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public String excluirListaDeTarefas(Long listaId) {
        EListasTarefas listaParaExcluir = this.listasTarefasInterface
                .findById(listaId)
                .orElseThrow(EntityNotFoundException::new);

        listaParaExcluir.setStatus(EnStatus.I);

        try {
            this.listasTarefasInterface.save(listaParaExcluir);
            return "lista de tarefas excluída com sucesso";
        } catch (Exception e) {
            log.error("Erro ao excluir tarefa: {}", e.getMessage(), e);
            return "erro ao excluir lista de tarefas";
        }
    }
}
