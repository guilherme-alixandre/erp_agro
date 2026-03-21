package br.com.gado.application.dto.usuarioDto;

import br.com.gado.domain.enums.EnPerfilUsuario;
import lombok.Data;

@Data
public class UsuarioCadastroDto {

    private String nome;
    private String email;
    private String senha;
    private EnPerfilUsuario perfil;

}
