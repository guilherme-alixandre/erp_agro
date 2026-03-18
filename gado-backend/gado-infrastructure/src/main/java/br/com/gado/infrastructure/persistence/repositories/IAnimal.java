package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface IAnimal extends JpaRepository<EAnimal, Long> {
    Optional<EAnimal> findByCodigoBrinco(String codigoBrinco);
    void deleteByCodigoBrinco(String codigoBrinco);
}
