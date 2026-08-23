package br.com.gado.dto.lancamentoFinanceiroDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Receita do mês agrupada por origem ("Leite" ou raça do animal vendido) — maior e menor fonte. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FontesReceitaDto {
    private String maiorFonteLabel;
    private BigDecimal maiorFonteValor;
    private String menorFonteLabel;
    private BigDecimal menorFonteValor;
}
