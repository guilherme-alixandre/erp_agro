package br.com.gado.dto.parcerioDto;

import br.com.gado.enums.EnTipoParceiro;
import lombok.Data;

@Data
public class ParceiroPutDto {
    private String nome;
    private String endereco;
    private String telefone;
    private EnTipoParceiro tipo;
}
