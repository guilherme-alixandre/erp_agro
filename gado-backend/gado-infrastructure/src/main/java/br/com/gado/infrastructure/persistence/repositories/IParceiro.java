package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EParceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IParceiro extends JpaRepository<EParceiro, Long> {
}
