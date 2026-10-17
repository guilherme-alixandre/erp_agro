package br.com.gado.dto.rentabilidadeDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Linha de "produtos mais vendidos" — uma por raça de animal vendido/abatido, mais um item "Leite". */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoVendidoDto {
    private String nome;
    private BigDecimal quantidade;
    private String unidade; // "cabeça(s)" | "litro(s)"
    private BigDecimal valorTotal;
}
