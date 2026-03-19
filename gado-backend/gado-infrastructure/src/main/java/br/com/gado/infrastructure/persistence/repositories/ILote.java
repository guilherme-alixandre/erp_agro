package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.ELote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ILote extends JpaRepository<ELote, Long> {}
