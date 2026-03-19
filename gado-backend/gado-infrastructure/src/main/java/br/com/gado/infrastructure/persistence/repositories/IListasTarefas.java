package br.com.gado.infrastructure.persistence.repositories;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IListasTarefas {
    Optional<IListasTarefas> findByListaTerafaId(Long id);
    void deleteByListaTerafaId(Long id);
}
