package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.ECategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICategoria extends JpaRepository<ECategoria, Long> {
    Optional<ECategoria> findById(Long id);
}
