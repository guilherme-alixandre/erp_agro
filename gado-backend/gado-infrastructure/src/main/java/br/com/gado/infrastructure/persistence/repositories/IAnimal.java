package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
public interface IAnimal extends JpaRepository<EAnimal, Long> {
    Optional<EAnimal> findByCodigoBrincoAndStatus(String codigoBrinco, EnStatus status);
    Optional<ArrayList<EAnimal>> findAllByStatus(EnStatus status);
    void deleteByCodigoBrinco(String codigoBrinco);
}
