package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.EListasTarefas;
import br.com.gado.domain.entities.ETarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITarefa extends JpaRepository<ETarefa, Long> {
    Optional<ETarefa> findByTarefaId(Long tarefaId);
    void deleteByTarefaId(Long tarefaId);
}
