package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnTipoParceiro;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class EParceiro extends EAbstract{

    private String nome;
    private String CPF_CNPJ;
    private String endereco;
    private String telefone;
    private LocalDateTime dataCadastro;

    @Enumerated(EnumType.STRING)
    private EnTipoParceiro tipo;

}
