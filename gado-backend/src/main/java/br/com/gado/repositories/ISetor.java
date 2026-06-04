package br.com.gado.repositories;

import br.com.gado.entities.ESetor;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public interface ISetor extends JpaRepository<ESetor, Long> {
    Optional<ESetor> findByIdAndStatus(Long id, EnStatus status);
    boolean existsByIdAndStatus(Long id, EnStatus status);
    ArrayList<ESetor> findAllByStatus(EnStatus status);
}
