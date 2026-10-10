package br.com.gado.repositories;

import br.com.gado.entities.ECustoAnimal;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICustoAnimal extends JpaRepository<ECustoAnimal, Long> {

    List<ECustoAnimal> findByAnimal_IdAndStatusOrderByDataEventoDesc(Long animalId, EnStatus status);

    List<ECustoAnimal> findByAnimal_IdInAndStatus(List<Long> animalIds, EnStatus status);

    List<ECustoAnimal> findByOrigemTipoAndOrigemIdAndStatus(String origemTipo, Long origemId, EnStatus status);
}
