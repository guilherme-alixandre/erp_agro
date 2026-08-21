package br.com.gado.repositories;

import br.com.gado.entities.EPagamentoFuncionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPagamentoFuncionario extends JpaRepository<EPagamentoFuncionario, Long> {
    Optional<EPagamentoFuncionario> findByFuncionarioIdAndAnoReferenciaAndMesReferencia(
            Long funcionarioId, Integer anoReferencia, Integer mesReferencia);
    List<EPagamentoFuncionario> findByAnoReferenciaAndMesReferencia(Integer anoReferencia, Integer mesReferencia);
}
