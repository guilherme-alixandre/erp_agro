package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EOcorrenciaAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IOcorrenciaAnimal extends JpaRepository<EOcorrenciaAnimal, Long> {
    Optional<EOcorrenciaAnimal> findByOcorrenciaId(Long id);
}
