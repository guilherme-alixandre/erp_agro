package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.ELoteSetor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ILoteSetor extends JpaRepository<ELoteSetor, Long> {

    List<ELoteSetor> findByLote_Id(Long loteId);

    List<ELoteSetor> findBySetor_Id(Long setorId);

    boolean existsByLote_IdAndSetor_Id(Long loteId, Long setorId);

    List<ELoteSetor> findByAnimais_IdAndLote_IdNot(Long animalId, Long loteId);
}
