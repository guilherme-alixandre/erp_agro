package br.com.gado.dto.grupoProdutoDto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Edição de um grupo já existente. Trocar o codigoPrefixo não reescreve os
 * codigoProduto já emitidos para produtos deste grupo — eles mantêm o prefixo antigo.
 */
@Data
public class GrupoProdutoPutDto {

    private String nome;

    @Pattern(regexp = "\\d{2}", message = "O prefixo deve conter exatamente 2 dígitos numéricos.")
    private String codigoPrefixo;
}
