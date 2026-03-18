package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IInsumo extends JpaRepository<EInsumo, Long> {
}
