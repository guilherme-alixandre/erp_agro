package br.com.gado.dto.folhaPagamentoDto;

import br.com.gado.enums.EnNaturezaFinanceira;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FuncionarioRespostaDto {
    private Long id;
    private String nomeCompleto;
    private String cpf;
    private String cargo;
    private LocalDate dataAdmissao;
    private LocalDate dataDemissao;
    private BigDecimal salarioBase;
    private BigDecimal percentualInss;
    private BigDecimal percentualFgts;
    private BigDecimal valorValeTransporte;
    private BigDecimal valorValeAlimentacao;
    private BigDecimal valorPlanoSaude;
    private EnNaturezaFinanceira naturezaFinanceira;
}
