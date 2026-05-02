package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface IUsuario extends JpaRepository<EUsuario, Long> {
    boolean existsByEmail(String email);
    Optional<EUsuario> findByEmailAndStatus(String email, EnStatus status);
    void deleteByEmail(String email);

    ArrayList<EUsuario> findAllByStatus(EnStatus enStatus);
}
