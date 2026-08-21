package br.com.gado.repositories;

import br.com.gado.entities.EDocumentoEntradaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDocumentoEntradaItem extends JpaRepository<EDocumentoEntradaItem, Long> {
}
