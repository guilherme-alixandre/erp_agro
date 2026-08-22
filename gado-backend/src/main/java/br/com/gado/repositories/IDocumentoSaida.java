package br.com.gado.repositories;

import br.com.gado.entities.EDocumentoSaida;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDocumentoSaida extends JpaRepository<EDocumentoSaida, Long> {
    List<EDocumentoSaida> findByStatusOrderByDataEmissaoDesc(EnStatus status);
}
