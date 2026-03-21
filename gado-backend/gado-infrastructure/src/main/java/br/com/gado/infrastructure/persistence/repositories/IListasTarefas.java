package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EListasTarefas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IListasTarefas extends JpaRepository<EListasTarefas, Long> {
    Optional<IListasTarefas> findByListaTerafaId(Long id);
    void deleteByListaTerafaId(Long id);
}
