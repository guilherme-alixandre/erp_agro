package br.com.gado.dto.loteDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Perda de alimentação acumulada de um lote — soma das sobras de ração NÃO reaproveitadas
 * (ver ESobraAlimentacao), rateada pelos animais do lote em cada setor, no mesmo princípio de
 * CustoRacaoLoteDto. Diferente do custo de ração normal, a perda ENTRA como despesa real no DRE
 * (ver SLancamentoFinanceiro.contabilizarPerdaAlimentacao) — uma perda não gera nenhum retorno.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerdaAlimentacaoLoteDto {
    private Long loteId;
    private BigDecimal valorTotalPerdido;
    private BigDecimal valorPerdidoPorAnimal;
}
