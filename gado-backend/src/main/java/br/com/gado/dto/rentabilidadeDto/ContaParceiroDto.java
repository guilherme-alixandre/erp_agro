package br.com.gado.dto.rentabilidadeDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Linha resumida de contas a pagar/receber por parceiro, dentro do período filtrado. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContaParceiroDto {
    private Long parceiroId;
    private String parceiroNome;
    private BigDecimal totalAPagar;
    private BigDecimal totalAReceber;
    private Integer qtdNfsPagar;
    private Integer qtdNfsReceber;
    private LocalDate proximoVencimento;
}
