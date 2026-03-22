package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EMovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IMovimentacaoEstoque extends JpaRepository<EMovimentacaoEstoque, Long> {
    Optional<EMovimentacaoEstoque> findByMovimentacaoEstoqueId(Long id);
}
