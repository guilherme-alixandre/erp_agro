package br.com.gado.repositories;

import br.com.gado.entities.EGrupoProduto;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IGrupoProduto extends JpaRepository<EGrupoProduto, Long> {
    Optional<EGrupoProduto> findFirstByNomeIgnoreCase(String nome);
    Optional<EGrupoProduto> findFirstByCodigoPrefixo(String codigoPrefixo);
    List<EGrupoProduto> findByStatusOrderByNomeAsc(EnStatus status);
    List<EGrupoProduto> findByStatusAndNomeContainingIgnoreCaseOrderByNomeAsc(EnStatus status, String nome);
    List<EGrupoProduto> findAllByOrderByNomeAsc();
    List<EGrupoProduto> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
}
