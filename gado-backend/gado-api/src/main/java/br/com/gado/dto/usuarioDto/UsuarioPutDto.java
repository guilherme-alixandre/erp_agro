package br.com.gado.dto.usuarioDto;

import br.com.gado.domain.enums.EnPerfilUsuario;
import lombok.Data;

@Data
public class UsuarioPutDto {
    private String nome;
    private EnPerfilUsuario perfil;
}
