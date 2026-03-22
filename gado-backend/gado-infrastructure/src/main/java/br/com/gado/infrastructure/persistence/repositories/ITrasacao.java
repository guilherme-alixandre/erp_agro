package br.com.gado.infrastructure.persistence.repositories;


import br.com.gado.domain.entities.ETransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITrasacao extends JpaRepository<ETransacao,Long> {
    Optional<ETransacao> findByTrasacaoId(Long id);
}
