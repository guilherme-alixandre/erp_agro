package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUsuario extends JpaRepository<EUsuario, Long> {}
