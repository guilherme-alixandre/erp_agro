package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EVacinacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IVacinacao extends JpaRepository<EVacinacao, Long> {
    Optional<EVacinacao> findByVacinacaoId(Long vacinacaoId);
    void deleteByVacinacaoId(Long vacinacaoId);
}
