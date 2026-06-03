package br.com.gado.repositories;

import br.com.gado.entities.ETarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITarefa extends JpaRepository<ETarefa, Long> {
    Optional<ETarefa> findById(Long tarefaId);
    void deleteById(Long tarefaId);
}
