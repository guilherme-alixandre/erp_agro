package br.com.gado.dto.lancamentoFinanceiroDto;

import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnTipoMovimentoFinanceiro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload de um lançamento manual (SLancamentoFinanceiro.registrarLancamentoManual) — para
 * movimentos ainda sem módulo de origem automatizado, ex: uma venda antes do módulo de Vendas
 * existir. naturezaFinanceira só deve vir preenchida quando tipoMovimento = SAIDA.
 */
@Data
public class LancamentoManualCadastroDto {

    @NotNull(message = "Informe se é uma ENTRADA ou uma SAIDA.")
    private EnTipoMovimentoFinanceiro tipoMovimento;

    private EnNaturezaFinanceira naturezaFinanceira;

    @NotBlank(message = "Descreva o lançamento.")
    private String descricao;

    @NotNull(message = "Informe o valor do lançamento.")
    private BigDecimal valor;

    @NotNull(message = "Informe a data de competência do lançamento.")
    private LocalDate dataCompetencia;
}
