package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.ESetor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISetor extends JpaRepository<ESetor, Long> {
}
