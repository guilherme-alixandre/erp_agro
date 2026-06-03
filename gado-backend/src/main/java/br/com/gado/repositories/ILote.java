package br.com.gado.repositories;

import br.com.gado.entities.ELote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ILote extends JpaRepository<ELote, Long> {
    Optional<ELote> findById(Long loteId);
}
