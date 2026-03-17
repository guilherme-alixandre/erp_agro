package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EPerfilUsuario;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class EntityUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    // criarei classe email ainda
    private String email;
    private String senha;

    @Enumerated(EnumType.STRING)
    private EPerfilUsuario perfil;

    private LocalDateTime dataCadastro;

}
