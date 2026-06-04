package br.com.gado.dto.usuarioDto;

import br.com.gado.dto.AbstractDTO;
import br.com.gado.enums.EnPerfilUsuario;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class UsuarioDto extends AbstractDTO {
    private String nome;
    private String email;
    private EnPerfilUsuario perfil;
    private LocalDateTime dataCadastro;
}
