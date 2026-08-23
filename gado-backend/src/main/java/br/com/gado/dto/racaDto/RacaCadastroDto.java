package br.com.gado.dto.racaDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RacaCadastroDto {

    @NotBlank(message = "O nome da raça é obrigatório.")
    private String nome;

    @NotBlank(message = "A sigla da raça é obrigatória.")
    @Pattern(regexp = "^[A-Za-z]{2,4}$", message = "A sigla deve ter de 2 a 4 letras.")
    private String sigla;
}
