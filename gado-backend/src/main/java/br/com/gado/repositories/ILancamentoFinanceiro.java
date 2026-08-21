package br.com.gado.repositories;

import br.com.gado.entities.ELancamentoFinanceiro;
import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnOrigemLancamentoFinanceiro;
import br.com.gado.enums.EnTipoMovimentoFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ILancamentoFinanceiro extends JpaRepository<ELancamentoFinanceiro, Long> {

    List<ELancamentoFinanceiro> findByAnoCompetenciaAndMesCompetenciaOrderByDataCompetenciaAsc(Integer ano, Integer mes);

    /** Usado para upsert: cada item de documento/pagamento gera no máximo um lançamento. */
    Optional<ELancamentoFinanceiro> findByOrigemAndOrigemId(EnOrigemLancamentoFinanceiro origem, Long origemId);

    @Query("select coalesce(sum(l.valor), 0) from ELancamentoFinanceiro l "
            + "where l.anoCompetencia = :ano and l.mesCompetencia = :mes and l.tipoMovimento = :tipo "
            + "and (:natureza is null or l.naturezaFinanceira = :natureza)")
    BigDecimal somarPorBloco(@Param("ano") int ano, @Param("mes") int mes,
                              @Param("tipo") EnTipoMovimentoFinanceiro tipo,
                              @Param("natureza") EnNaturezaFinanceira natureza);
}
