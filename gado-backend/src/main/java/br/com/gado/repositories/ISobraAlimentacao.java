package br.com.gado.repositories;

import br.com.gado.entities.ESobraAlimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISobraAlimentacao extends JpaRepository<ESobraAlimentacao, Long> {

    Optional<ESobraAlimentacao> findByConsumoInsumo_Id(Long consumoInsumoId);

    /** Evita N+1 ao montar a listagem de "Alimentar Setores" e o rateio de perdas por lote. */
    List<ESobraAlimentacao> findByConsumoInsumo_IdIn(List<Long> consumoInsumoIds);
}
