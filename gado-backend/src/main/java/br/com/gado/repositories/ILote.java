package br.com.gado.repositories;

import br.com.gado.entities.ELote;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public interface ILote extends JpaRepository<ELote, Long> {

    Optional<ELote> findByIdAndStatus(Long id, EnStatus status);

    ArrayList<ELote> findAllByStatus(EnStatus status);

    /**
     * Busca o maior código existente para gerar o próximo incremento (ex: "LOT042").
     */
    @Query("SELECT l.codigo FROM ELote l ORDER BY l.codigo DESC LIMIT 1")
    Optional<String> findUltimoCodigoGerado();
}
