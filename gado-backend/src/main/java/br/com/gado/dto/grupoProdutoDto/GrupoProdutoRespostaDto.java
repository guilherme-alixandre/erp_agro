package br.com.gado.dto.grupoProdutoDto;

import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoInsumo;
import lombok.Data;

@Data
public class GrupoProdutoRespostaDto {
    private Long id;
    private String nome;
    private String codigoPrefixo;
    private EnTipoInsumo categoriaGrupo;
    private EnNaturezaFinanceira naturezaFinanceira;
    private EnStatus status;
}
