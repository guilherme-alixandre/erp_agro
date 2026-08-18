package br.com.gado.repositories;

import br.com.gado.entities.EConsumoInsumo;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IConsumoInsumo extends JpaRepository<EConsumoInsumo, Long> {

    List<EConsumoInsumo> findBySetor_IdAndStatusOrderByDataConsumoDesc(Long setorId, EnStatus status);

    List<EConsumoInsumo> findByInsumo_IdAndStatusOrderByDataConsumoDesc(Long insumoId, EnStatus status);
}
