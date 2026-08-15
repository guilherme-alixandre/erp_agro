package br.com.gado.repositories;

import br.com.gado.entities.EMetaSetor;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IMetaSetor extends JpaRepository<EMetaSetor, Long> {

    List<EMetaSetor> findBySetor_IdAndStatus(Long setorId, EnStatus status);

    Optional<EMetaSetor> findByIdAndStatus(Long id, EnStatus status);
}
