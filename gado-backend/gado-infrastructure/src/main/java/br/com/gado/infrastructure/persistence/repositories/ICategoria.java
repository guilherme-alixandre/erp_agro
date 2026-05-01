package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.ECategoria;
import br.com.gado.domain.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICategoria extends JpaRepository<ECategoria, Long> {
    Optional<ECategoria> findByIdAndStatus(Long id, EnStatus status);
}
