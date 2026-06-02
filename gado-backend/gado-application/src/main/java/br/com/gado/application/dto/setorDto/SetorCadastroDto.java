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
    private int capacidadeMaxima;
    private String metaTexto;
    private Double metaProducaoLeite;
    private Double metaArrobaAbate;
    private br.com.gado.domain.enums.EnTipoSetor setor;
}
