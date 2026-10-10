package br.com.gado.dto.loteDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Custo total acumulado de um lote — soma do ledger de custo (ver ECustoAnimal/SCustoAnimal) de
 * cada animal ATUALMENTE alocado ao lote, em todas as categorias (aquisição, alimentação,
 * vacinação). Diferente de CustoRacaoLoteDto (só ração, recalculado pelo preço atual), aqui os
 * valores já vêm fotografados no momento de cada evento. Como o ledger pertence ao animal, um
 * animal que chegou recentemente ao lote traz consigo todo o seu histórico de custo anterior.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustoLoteDto {
    private Long loteId;
    private BigDecimal totalAquisicao;
    private BigDecimal totalAlimentacao;
    private BigDecimal totalVacinacao;
    private BigDecimal totalGeral;
    private BigDecimal aquisicaoPorAnimal;
    private BigDecimal alimentacaoPorAnimal;
    private BigDecimal vacinacaoPorAnimal;
    private BigDecimal totalPorAnimal;
}
