package br.com.gado.dto.grupoProdutoDto;

import br.com.gado.enums.EnNaturezaFinanceira;
import lombok.Data;

@Data
public class GrupoProdutoRespostaDto {
    private Long id;
    private String nome;
    private String codigoPrefixo;
    private EnNaturezaFinanceira naturezaFinanceira;
}
