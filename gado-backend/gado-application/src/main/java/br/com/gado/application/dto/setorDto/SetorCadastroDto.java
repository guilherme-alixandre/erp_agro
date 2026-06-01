package br.com.gado.application.dto.setorDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetorCadastroDto {
    private String descricao;
    private Long usuario_id;
}
