package br.com.gado.dto.parcerioDto;

import br.com.gado.enums.EnTipoParceiro;
import lombok.Data;

@Data
public class ParceiroCadastroDto {
    private String nome;
    private String CPF_CNPJ;
    private String endereco;
    private String telefone;
    private EnTipoParceiro tipo;

}
