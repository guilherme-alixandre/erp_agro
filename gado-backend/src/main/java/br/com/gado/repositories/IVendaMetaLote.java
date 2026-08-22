package br.com.gado.repositories;

import br.com.gado.entities.EVendaMetaLote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IVendaMetaLote extends JpaRepository<EVendaMetaLote, Long> {
    List<EVendaMetaLote> findByMetaSetor_Id(Long metaSetorId);
}
