package br.com.gado.repositories;

import br.com.gado.entities.EDocumentoEntrada;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnStatusAprovacaoFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDocumentoEntrada extends JpaRepository<EDocumentoEntrada, Long> {
    Optional<EDocumentoEntrada> findByChaveAcessoNfe(String chaveAcessoNfe);
    List<EDocumentoEntrada> findByStatusAprovacaoOrderByDataEntradaDesc(EnStatusAprovacaoFinanceira status);
    List<EDocumentoEntrada> findAllByOrderByDataEntradaDesc();
    List<EDocumentoEntrada> findByStatusOrderByDataEntradaDesc(EnStatus status);
}
