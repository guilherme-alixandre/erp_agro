package br.com.gado.repositories;

import br.com.gado.entities.EUnidadeMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUnidadeMedida extends JpaRepository<EUnidadeMedida, Long> {
    Optional<EUnidadeMedida> findById(Long id);
}
