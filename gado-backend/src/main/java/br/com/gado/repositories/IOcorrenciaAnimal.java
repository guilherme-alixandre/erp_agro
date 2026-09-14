package br.com.gado.repositories;

import br.com.gado.entities.EOcorrenciaAnimal;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IOcorrenciaAnimal extends JpaRepository<EOcorrenciaAnimal, Long> {
    Optional<EOcorrenciaAnimal> findByIdAndStatus(Long id, EnStatus status);

    List<EOcorrenciaAnimal> findByIdAnimal_IdAndStatusOrderByDataOcorrenciaDesc(Long animalId, EnStatus status);
}
