package br.com.gado.repositories;

import br.com.gado.entities.ERaca;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRaca extends JpaRepository<ERaca, Long> {
    Optional<ERaca> findFirstByNomeIgnoreCase(String nome);
    Optional<ERaca> findFirstBySiglaIgnoreCase(String sigla);
    List<ERaca> findByStatusOrderByNomeAsc(EnStatus status);
    List<ERaca> findByStatusAndNomeContainingIgnoreCaseOrderByNomeAsc(EnStatus status, String nome);
    List<ERaca> findAllByOrderByNomeAsc();
    List<ERaca> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
}
