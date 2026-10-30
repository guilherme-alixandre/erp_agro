package br.com.gado.dto.usuarioDto;

import br.com.gado.enums.EnPerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioCadastroDto {

    private String nome;

    @NotBlank(message = "Digite um e-mail válido.")
    @Email(message = "Digite um e-mail válido.")
    private String email;

    @NotBlank(message = "A senha deve possuir 6 ou mais caracteres.")
    @Size(min = 6, message = "A senha deve possuir 6 ou mais caracteres.")
    private String senha;

    private EnPerfilUsuario perfil;

}
