package br.com.gado.repositories;

import br.com.gado.entities.EListasTarefas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IListasTarefas extends JpaRepository<EListasTarefas, Long> {
    void deleteById(Long id);
}
