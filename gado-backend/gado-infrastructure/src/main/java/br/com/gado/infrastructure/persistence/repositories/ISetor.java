package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.ESetor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ISetor extends JpaRepository<ESetor, Long> {
    @Query("SELECT s FROM ESetor s WHERE (:id IS NULL OR s.id = :id) AND (:descricao IS NULL OR LOWER(s.descricao) LIKE LOWER(CONCAT('%', :descricao, '%')))")
    Page<ESetor> findByIdAndDescricaoPartialMatch(@Param("id") Long id, @Param("descricao") String descricao, Pageable pageable);
}
