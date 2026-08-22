package br.com.gado.repositories;

import br.com.gado.entities.EConsumoInsumo;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IConsumoInsumo extends JpaRepository<EConsumoInsumo, Long> {

    List<EConsumoInsumo> findBySetor_IdAndStatusOrderByDataConsumoDesc(Long setorId, EnStatus status);

    List<EConsumoInsumo> findByInsumo_IdAndStatusOrderByDataConsumoDesc(Long insumoId, EnStatus status);

    List<EConsumoInsumo> findBySetor_IdAndStatusAndDataConsumoBetweenOrderByDataConsumoDesc(
            Long setorId, EnStatus status, LocalDateTime inicio, LocalDateTime fim);

    interface ResumoInsumoProjection {
        Long getInsumoId();
        Double getTotal();
    }

    @Query("SELECT c.insumo.id AS insumoId, SUM(c.quantidadeBaixaUnidadePrimaria) AS total "
            + "FROM EConsumoInsumo c WHERE c.setor.id = :setorId AND c.status = :status "
            + "AND c.dataConsumo BETWEEN :inicio AND :fim GROUP BY c.insumo.id")
    List<ResumoInsumoProjection> resumoPorSetorEPeriodo(
            @Param("setorId") Long setorId,
            @Param("status") EnStatus status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);
}
