package br.com.gado.entities;

import br.com.gado.enums.EnPerfilUsuario;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "usuario")
@Data
public class EUsuario extends EAbstract{

    private String nome;
    private String email;
    private String senha;

    @Enumerated(EnumType.STRING)
    private EnPerfilUsuario perfil;

    private LocalDateTime dataCadastro;

}
