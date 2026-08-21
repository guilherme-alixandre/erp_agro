package br.com.gado.dto.lancamentoFinanceiroDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Resultado do "DRE" mensal — SLancamentoFinanceiro.gerarResumoMensal(ano, mes). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumoMensalDto {
    private int ano;
    private int mes;
    private BigDecimal totalEntradas;
    private BigDecimal totalSaidasCusto;
    private BigDecimal totalSaidasDespesa;
    private BigDecimal totalSaidas;
    private BigDecimal lucroLiquido;
}
