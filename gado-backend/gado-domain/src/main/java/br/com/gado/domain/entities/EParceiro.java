package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnTipoParceiro;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class EParceiro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String CPF_CNPJ;
    private String endereco;
    private String telefone;
    private LocalDateTime dataCadastro;

    @Enumerated(EnumType.STRING)
    private EnTipoParceiro tipo;

}
