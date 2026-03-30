package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.ERegistroFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRegistroFinanceiro extends JpaRepository<ERegistroFinanceiro, Long> {
    Optional<ERegistroFinanceiro> findById(Long id);
}
