package br.com.gado.application.services;

import br.com.gado.domain.entities.EListasTarefas;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.dto.ListasTarefasDTO;
import br.com.gado.infrastructure.persistence.repositories.IListasTarefas;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

@Service
public class SListasTarefas {

    private static final Logger log = LoggerFactory.getLogger(SListasTarefas.class);
    private final IListasTarefas listasTarefasInterface;
    private final ModelMapper modelMapper;
    private final TransactionalOperator transactionalOperator;

    public SListasTarefas(IListasTarefas listasTarefas, TransactionalOperator transactionalOperator) {
        this.listasTarefasInterface = listasTarefas;
        this.modelMapper = new ModelMapper();
        this.transactionalOperator = transactionalOperator;
    }

    @Transactional
    public ListasTarefasDTO criarListaDeTarefas (ListasTarefasDTO dadosLista) {

        EListasTarefas listaTarefas = new EListasTarefas();
        listaTarefas.setNomeLista(dadosLista.getNomeLista());

        EListasTarefas listaSalva = listasTarefasInterface.save(listaTarefas);

        return modelMapper.map(listaSalva, ListasTarefasDTO.class);
    }

    @Transactional
    public ListasTarefasDTO buscarListaDeTarefasPorId(Long listaId) {
        EListasTarefas listaTarefaEntitye = (EListasTarefas) this.listasTarefasInterface
                .findByListaTerafaId(listaId)
                .orElseThrow(EntityNotFoundException::new);
        return modelMapper.map(listaTarefaEntitye, ListasTarefasDTO.class);
    }

    @Transactional
    public ListasTarefasDTO atualizarListaDeTarefasPorId(ListasTarefasDTO listaParaAtualizar, Long listaId) {
        EListasTarefas existingEntitye = (EListasTarefas) this.listasTarefasInterface
                .findByListaTerafaId(listaId)
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);

        this.modelMapper.map(listaParaAtualizar, existingEntitye);
        EListasTarefas listaParaSalvar = existingEntitye;

        try {

            EListasTarefas listaSalva = this.listasTarefasInterface.save(listaParaSalvar);
            return modelMapper.map(listaSalva, ListasTarefasDTO.class);

        } catch (Exception e){
            log.error("Erro ao atualizar tarefa: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public boolean excluirListaDeTarefas(Long listaId) {
        EListasTarefas listaParaExcluir = (EListasTarefas) this.listasTarefasInterface
                .findByListaTerafaId(listaId)
                .orElseThrow(EntityNotFoundException::new);

        listaParaExcluir.setStatus(EnStatus.I);

        try{
            this.listasTarefasInterface.save(listaParaExcluir);
            return true;
        } catch (Exception e){
            log.error("Erro ao excluir tarefa: {}", e.getMessage(), e);
        }

        return false;
    }
}
