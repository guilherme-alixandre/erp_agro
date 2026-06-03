package br.com.gado.repositories;

import br.com.gado.entities.EAnimal;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
public interface IAnimal extends JpaRepository<EAnimal, Long> {
    Optional<EAnimal> findByCodigoBrincoAndStatus(String codigoBrinco, EnStatus status);
    Optional<ArrayList<EAnimal>> findAllByStatus(EnStatus status);
    Boolean existsByCodigoBrincoAndStatus(String codigoBrinco, EnStatus status);
    void deleteByCodigoBrinco(String codigoBrinco);
}
