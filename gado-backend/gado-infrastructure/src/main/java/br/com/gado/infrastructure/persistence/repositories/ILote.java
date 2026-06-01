package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.ELote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ILote extends JpaRepository<ELote, Long> {
    Optional<ELote> findById(Long loteId);

    @Query("SELECT l FROM ELote l WHERE (:id IS NULL OR l.id = :id) AND (:descricao IS NULL OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :descricao, '%')))")
    List<ELote> findByIdAndDescricaoPartialMatch(@Param("id") Long id, @Param("descricao") String descricao);
}
