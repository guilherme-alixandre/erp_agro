package br.com.gado.dto.lancamentoFinanceiroDto;

import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnOrigemLancamentoFinanceiro;
import br.com.gado.enums.EnTipoMovimentoFinanceiro;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LancamentoFinanceiroRespostaDto {
    private Long id;
    private EnTipoMovimentoFinanceiro tipoMovimento;
    private EnNaturezaFinanceira naturezaFinanceira;
    private EnOrigemLancamentoFinanceiro origem;
    private Long origemId;
    private String descricao;
    private BigDecimal valor;
    private LocalDate dataCompetencia;
    private Boolean virtual;
    private String criadoPorEmail;
}
