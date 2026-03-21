package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnPerfilUsuario;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class EUsuario extends EAbstract{

    private String nome;
    // criarei classe email ainda
    private String email;
    private String senha;

    @Enumerated(EnumType.STRING)
    private EnPerfilUsuario perfil;

    private LocalDateTime dataCadastro;

}
