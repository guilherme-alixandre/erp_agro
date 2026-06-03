package br.com.gado.repositories;

import br.com.gado.entities.EOcorrenciaAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IOcorrenciaAnimal extends JpaRepository<EOcorrenciaAnimal, Long> {
    Optional<EOcorrenciaAnimal> findById(Long id);
}
