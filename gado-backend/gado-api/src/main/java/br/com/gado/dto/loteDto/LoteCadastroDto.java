package br.com.gado.dto;

import lombok.Data;

@Data
public class LoteCadastroDto {
    private String descricao;
    private String racaPredominante;
    private Long usuario_id;
}
