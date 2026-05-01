package br.com.gado.application.dto.usuarioDto;

import br.com.gado.domain.enums.EnPerfilUsuario;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsuarioRespostaDto {
    private String nome;
    private String email;
    private EnPerfilUsuario perfil;
    private LocalDateTime dataCadastro;
}
