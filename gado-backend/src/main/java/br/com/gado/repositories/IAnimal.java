package br.com.gado.repositories;

import br.com.gado.entities.EAnimal;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
public interface IAnimal extends JpaRepository<EAnimal, Long> {
    Optional<EAnimal> findByCodigoBrincoAndStatus(String codigoBrinco, EnStatus status);
    Optional<EAnimal> findByIdAndStatus(Long id, EnStatus status);
    Optional<ArrayList<EAnimal>> findAllByStatus(EnStatus status);
    Boolean existsByCodigoBrincoAndStatus(String codigoBrinco, EnStatus status);
    void deleteByCodigoBrinco(String codigoBrinco);

    /** Maior codigoBrinco já emitido para uma sigla de raça (ex: prefixo "NE" → achar "NE0007"). */
    Optional<EAnimal> findFirstByCodigoBrincoStartingWithOrderByCodigoBrincoDesc(String prefixo);

    @Query("SELECT a FROM EAnimal a WHERE a.status = :status AND ("
            + "LOWER(a.codigoBrinco) LIKE LOWER(CONCAT('%', :termo, '%')) "
            + "OR LOWER(a.raca.nome) LIKE LOWER(CONCAT('%', :termo, '%')))")
    List<EAnimal> buscarPorTermo(@Param("status") EnStatus status, @Param("termo") String termo);
}
