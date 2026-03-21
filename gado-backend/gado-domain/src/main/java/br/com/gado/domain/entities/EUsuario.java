package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnPerfilUsuario;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class EUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String senha;

    @Enumerated(EnumType.STRING)
    private EnPerfilUsuario perfil;

    private LocalDateTime dataCadastro;

}
