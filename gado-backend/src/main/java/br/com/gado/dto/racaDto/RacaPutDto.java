package br.com.gado.dto.racaDto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RacaPutDto {

    private String nome;

    @Pattern(regexp = "^[A-Za-z]{2,4}$", message = "A sigla deve ter de 2 a 4 letras.")
    private String sigla;
}
