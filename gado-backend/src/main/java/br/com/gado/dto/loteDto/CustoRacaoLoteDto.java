package br.com.gado.dto.loteDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Custo de ração acumulado de um lote — informativo, para ajudar na precificação do lote/animal.
 * NÃO é somado de novo como despesa no DRE (a compra da ração já foi contabilizada como despesa
 * do mês na NF de entrada — ver SLancamentoFinanceiro.gerarResumoMensal).
 *
 * Calculado a partir do rateio de "Alimentar Setores" (EConsumoInsumo.consumoPorAnimal) pela
 * quantidade ATUAL de animais do lote em cada setor, multiplicado pelo preço médio ATUAL do
 * insumo. É uma aproximação: não há histórico de qual lote estava em cada setor no momento exato
 * de cada consumo, nem do preço médio do insumo naquele momento.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustoRacaoLoteDto {
    private Long loteId;
    private BigDecimal custoTotalAcumulado;
    private BigDecimal custoPorAnimal;
}
