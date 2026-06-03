package br.com.gado.repositories;

import br.com.gado.entities.EInsumo;
import br.com.gado.enums.EnTipoInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IInsumo extends JpaRepository<EInsumo, Long> {
    Optional<EInsumo> findFirstByNomeIgnoreCase(String nome);

    List<EInsumo> findByTipoOrderByNomeAsc(EnTipoInsumo tipo);

    List<EInsumo> findByTipoAndNomeContainingIgnoreCaseOrderByNomeAsc(
            EnTipoInsumo tipo, String nome);
}
