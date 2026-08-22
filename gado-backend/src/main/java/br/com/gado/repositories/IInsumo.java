package br.com.gado.repositories;

import br.com.gado.entities.EInsumo;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IInsumo extends JpaRepository<EInsumo, Long> {
    Optional<EInsumo> findFirstByNomeIgnoreCase(String nome);

    // ── Estoque (Catálogo geral de Insumos) ──────────────────────────────

    Optional<EInsumo> findByIdAndStatus(Long id, EnStatus status);

    List<EInsumo> findByStatusOrderByNomeAsc(EnStatus status);

    List<EInsumo> findByStatusAndNomeContainingIgnoreCaseOrderByNomeAsc(EnStatus status, String nome);

    List<EInsumo> findAllByOrderByNomeAsc();

    List<EInsumo> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    // ── Catálogo de Produtos: geração de código sequencial ───────────────

    /**
     * Maior codigoProduto já emitido dentro da faixa do grupo (ex: entre "01000000" e "01999999").
     * O intervalo fechado (BETWEEN) permite ao Postgres resolver a consulta com um range scan
     * no índice único de codigo_produto — ORDER BY DESC + LIMIT 1 encontra o maior valor da faixa
     * sem varrer a tabela inteira e sem depender de LIKE/collation para casar o prefixo.
     */
    Optional<EInsumo> findFirstByCodigoProdutoBetweenOrderByCodigoProdutoDesc(String codigoInicio, String codigoFim);
}
