package br.com.gado.dto.rentabilidadeDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Linha de "maiores gastos por grupo" — folha de pagamento, grupos de produto comprados/consumidos, etc. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GastoGrupoDto {
    private String grupoNome;
    private BigDecimal valorTotal;
}
