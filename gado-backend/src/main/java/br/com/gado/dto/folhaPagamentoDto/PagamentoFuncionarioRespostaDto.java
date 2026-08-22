package br.com.gado.dto.folhaPagamentoDto;

import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnStatusDespesa;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PagamentoFuncionarioRespostaDto {
    private Long id;
    private Long funcionarioId;
    private String funcionarioNome;
    private Integer anoReferencia;
    private Integer mesReferencia;
    private LocalDate dataPagamento;
    private EnStatusDespesa statusPagamento;
    private BigDecimal valorBruto;
    private BigDecimal descontoInss;
    private BigDecimal descontoOutros;
    private BigDecimal encargoFgts;
    private BigDecimal valorBeneficios;
    private BigDecimal valorBonus;
    private BigDecimal valorLiquido;
    private EnNaturezaFinanceira naturezaFinanceiraSnapshot;
    private Boolean estornado;
    private String motivoEstorno;
    private String estornadoPorEmail;
    private LocalDateTime estornadoEm;
}
