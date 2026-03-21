package br.com.gado.application.dto.loteDto;

import lombok.Data;

@Data
public class LoteCadastroDto {
    private String descricao;
    private String racaPredominante;
    private Long usuario_id;
}
