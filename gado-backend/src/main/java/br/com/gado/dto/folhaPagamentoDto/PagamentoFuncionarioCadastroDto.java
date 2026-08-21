package br.com.gado.dto.folhaPagamentoDto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lançamento da folha de um funcionário para um Bloco Ano/Mês. valorBruto, descontoInss,
 * encargoFgts e valorBeneficios são calculados por SFolhaPagamento a partir do cadastro
 * do funcionário — aqui só se informa a referência e os ajustes do mês.
 */
@Data
public class PagamentoFuncionarioCadastroDto {

    @NotNull(message = "Informe o funcionário.")
    private Long funcionarioId;

    @NotNull(message = "Informe o ano de referência.")
    private Integer anoReferencia;

    @NotNull(message = "Informe o mês de referência.")
    @Min(value = 1, message = "Mês inválido.")
    @Max(value = 12, message = "Mês inválido.")
    private Integer mesReferencia;

    @NotNull(message = "Informe a data de pagamento.")
    private LocalDate dataPagamento;

    /** Descontos extras do mês (faltas, adiantamentos), além do INSS calculado automaticamente. */
    private BigDecimal descontoOutros;
}
