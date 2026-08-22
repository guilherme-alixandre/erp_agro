package br.com.gado.dto.folhaPagamentoDto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Edição de funcionário. CPF não é editável; natureza financeira é sempre CUSTO (não editável aqui). */
@Data
public class FuncionarioPutDto {

    private String nomeCompleto;
    private String cargo;
    private LocalDate dataDemissao;
    private BigDecimal salarioBase;
    private BigDecimal percentualInss;
    private BigDecimal percentualFgts;
    private BigDecimal valorValeTransporte;
    private BigDecimal valorValeAlimentacao;
    private BigDecimal valorPlanoSaude;
}
