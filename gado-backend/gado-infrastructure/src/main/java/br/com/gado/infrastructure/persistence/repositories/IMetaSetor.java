package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EMetaSetor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMetaSetor extends JpaRepository<EMetaSetor, Long> {

    List<EMetaSetor> findBySetor_Id(Long setorId);
}
