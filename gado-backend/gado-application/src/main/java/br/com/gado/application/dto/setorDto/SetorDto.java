package br.com.gado.application.dto.setorDto;

import br.com.gado.domain.enums.EnTipoSetor;
import lombok.Data;

@Data
public class SetorDto {
    private String nome;
    private int capacidadeMaxima;
    private String metaTexto;
    private Double metaProducaoLeite;
    private Double metaArrobaAbate;
    private EnTipoSetor setor;

}
