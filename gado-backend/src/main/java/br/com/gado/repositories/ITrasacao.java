package br.com.gado.repositories;


import br.com.gado.entities.ETransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITrasacao extends JpaRepository<ETransacao,Long> {
    Optional<ETransacao> findById(Long id);
}
