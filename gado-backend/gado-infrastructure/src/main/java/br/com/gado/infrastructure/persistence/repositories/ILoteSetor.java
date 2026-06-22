package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.ELoteSetor;
import br.com.gado.domain.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ILoteSetor extends JpaRepository<ELoteSetor, Long> {

    List<ELoteSetor> findByLote_Id(Long loteId);

    List<ELoteSetor> findBySetor_Id(Long setorId);

    boolean existsByLote_IdAndSetor_Id(Long loteId, Long setorId);

    java.util.Optional<ELoteSetor> findByLote_IdAndSetor_Id(Long loteId, Long setorId);

    // Busca conflitos apenas em lotes ATIVOS, ignorando registros de lotes inativados
    @Query("SELECT ls FROM ELoteSetor ls JOIN ls.animais a " +
           "WHERE a.id = :animalId AND ls.lote.id <> :loteId AND ls.lote.status = :statusAtivo")
    List<ELoteSetor> findConflitosAtivos(
            @Param("animalId") Long animalId,
            @Param("loteId") Long loteId,
            @Param("statusAtivo") EnStatus statusAtivo);

    // Retorna todas as alocações ativas de um animal (para localizar origem em transferências)
    @Query("SELECT ls FROM ELoteSetor ls JOIN ls.animais a " +
           "WHERE a.id = :animalId AND ls.lote.status = :statusAtivo")
    List<ELoteSetor> findByAnimalIdAndLoteAtivo(
            @Param("animalId") Long animalId,
            @Param("statusAtivo") EnStatus statusAtivo);
}
