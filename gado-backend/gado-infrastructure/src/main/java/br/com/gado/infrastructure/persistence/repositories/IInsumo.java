package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EInsumo;
import br.com.gado.domain.enums.EnTipoInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IInsumo extends JpaRepository<EInsumo, Long> {
    Optional<EInsumo> findFirstByNomeIgnoreCase(String nome);

    Optional<EInsumo> findFirstByTipoAndNomeIgnoreCase(EnTipoInsumo tipo, String nome);

    List<EInsumo> findByTipoOrderByNomeAsc(EnTipoInsumo tipo);

    List<EInsumo> findByTipoAndNomeContainingIgnoreCaseOrderByNomeAsc(
            EnTipoInsumo tipo, String nome);
}
