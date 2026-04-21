package br.com.gado.application.dto.parcerioDto;

import br.com.gado.application.dto.AbstractDTO;
import br.com.gado.domain.enums.EnTipoParceiro;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class ParceiroDto extends AbstractDTO {
    private String nome;
    private String cpfCnpj;
    private String endereco;
    private String telefone;
    private LocalDateTime dataCadastro;
    private EnTipoParceiro tipo;
}
