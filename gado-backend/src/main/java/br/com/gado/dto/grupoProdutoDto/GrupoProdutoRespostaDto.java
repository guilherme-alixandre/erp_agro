package br.com.gado.dto.grupoProdutoDto;

import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnStatus;
import lombok.Data;

@Data
public class GrupoProdutoRespostaDto {
    private Long id;
    private String nome;
    private String codigoPrefixo;
    private EnNaturezaFinanceira naturezaFinanceira;
    private EnStatus status;
}
