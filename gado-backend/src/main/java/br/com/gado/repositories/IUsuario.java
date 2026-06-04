package br.com.gado.repositories;

import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface IUsuario extends JpaRepository<EUsuario, Long> {
    boolean existsByEmailAndStatus(String email, EnStatus status);
    Optional<EUsuario> findByEmailAndStatus(String email, EnStatus status);
    void deleteByEmail(String email);

    ArrayList<EUsuario> findAllByStatus(EnStatus enStatus);
}
