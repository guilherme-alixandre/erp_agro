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

    Optional<ELote> findByPadraoTrueAndStatus(EnStatus status);

    /**
     * Busca o maior código LOT* existente para gerar o próximo incremento.
     * Filtra apenas códigos com prefixo LOT para não confundir com 'PADRAO'.
     */
    @Query("SELECT l.codigo FROM ELote l WHERE l.codigo LIKE 'LOT%' ORDER BY l.codigo DESC LIMIT 1")
    Optional<String> findUltimoCodigoGerado();
}
