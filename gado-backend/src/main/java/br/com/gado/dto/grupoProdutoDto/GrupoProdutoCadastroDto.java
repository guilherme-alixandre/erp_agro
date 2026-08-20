package br.com.gado.dto.grupoProdutoDto;

import br.com.gado.enums.EnNaturezaFinanceira;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GrupoProdutoCadastroDto {

    @NotBlank(message = "O nome do grupo é obrigatório.")
    private String nome;

    @NotBlank(message = "O prefixo do grupo é obrigatório.")
    @Pattern(regexp = "\\d{2}", message = "O prefixo deve conter exatamente 2 dígitos numéricos.")
    private String codigoPrefixo;

    @NotNull(message = "A natureza financeira do grupo é obrigatória.")
    private EnNaturezaFinanceira naturezaFinanceira;
}
