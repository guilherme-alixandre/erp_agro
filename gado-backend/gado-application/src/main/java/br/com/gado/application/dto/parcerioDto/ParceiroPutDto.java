package br.com.gado.application.dto.parcerioDto;

import br.com.gado.domain.enums.EnTipoParceiro;
import lombok.Data;

@Data
public class ParceiroPutDto {
    private String nome;
    private String endereco;
    private String telefone;
    private EnTipoParceiro tipo;
}
