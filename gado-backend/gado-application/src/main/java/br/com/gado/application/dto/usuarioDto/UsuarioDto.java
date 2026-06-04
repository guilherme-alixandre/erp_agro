package br.com.gado.application.dto.usuarioDto;

import br.com.gado.application.dto.AbstractDTO;
import br.com.gado.domain.enums.EnPerfilUsuario;
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
