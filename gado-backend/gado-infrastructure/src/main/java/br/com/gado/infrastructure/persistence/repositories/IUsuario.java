package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUsuario extends JpaRepository<EUsuario, Long> {
    boolean existByEmail(String email);
    Optional<EUsuario> findByEmail(String email);
    void deleteByEmail(String email);
}
