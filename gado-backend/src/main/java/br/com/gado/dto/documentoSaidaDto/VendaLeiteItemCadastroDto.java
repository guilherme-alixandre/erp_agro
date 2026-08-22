package br.com.gado.dto.documentoSaidaDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VendaLeiteItemCadastroDto {

    @NotNull(message = "Informe o lote de origem do leite.")
    private Long loteId;

    @NotNull(message = "Informe os litros vendidos deste lote.")
    @Positive(message = "Os litros vendidos devem ser maiores que zero.")
    private BigDecimal litros;
}
