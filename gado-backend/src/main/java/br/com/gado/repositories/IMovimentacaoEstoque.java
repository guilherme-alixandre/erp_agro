package br.com.gado.repositories;

import br.com.gado.entities.EMovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IMovimentacaoEstoque extends JpaRepository<EMovimentacaoEstoque, Long> {
    Optional<EMovimentacaoEstoque> findById(Long id);
    List<EMovimentacaoEstoque> findAllByOrderByDataMovimentacaoDesc();
    List<EMovimentacaoEstoque> findByInsumoId_IdOrderByDataMovimentacaoDesc(Long insumoId);
}
