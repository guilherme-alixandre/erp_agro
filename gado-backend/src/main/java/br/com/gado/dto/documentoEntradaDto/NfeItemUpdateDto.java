package br.com.gado.dto.documentoEntradaDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** Ajuste pontual de um item já importado — usado somente dentro de NfeUpdateDto. */
@Data
public class NfeItemUpdateDto {

    @NotNull(message = "Informe o item a ser corrigido.")
    private Long itemId;

    private BigDecimal quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
}
