package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EUnidadeMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUnidadeMedida extends JpaRepository<EUnidadeMedida, Long> {
    Optional<EUnidadeMedida> findByUnidadeMedidaId(Long id);
}
