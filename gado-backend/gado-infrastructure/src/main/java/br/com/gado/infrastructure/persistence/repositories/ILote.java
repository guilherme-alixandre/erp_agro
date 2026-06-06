package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface ILote extends JpaRepository<ELote, Long> {

    Optional<ELote> findByCodigoAndStatus(String codigo, EnStatus status);

    boolean existsByCodigoAndStatus(String codigo, EnStatus status);

    ArrayList<ELote> findAllByStatus(EnStatus status);

    Optional<ELote> findByIdAndStatus(Long id, EnStatus status);

    /**
     * Busca o maior código existente para gerar o próximo incremento.
     * Retorna algo como "LOT042" para que o Service extraia o número.
     */
    @Query("SELECT l.codigo FROM ELote l ORDER BY l.codigo DESC LIMIT 1")
    Optional<String> findUltimoCodigoGerado();
}
