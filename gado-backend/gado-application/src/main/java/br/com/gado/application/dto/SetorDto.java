package br.com.gado.application.dto;

import br.com.gado.domain.enums.EnTipoSetor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SetorDto extends AbstractDTO{
    private String nome;
    private int capacidadeMaxima;
    private String metaTexto;
    private Double metaProducaoLeite;
    private Double metaArrobaAbate;
    private EnTipoSetor tipo;

    private String criadoPorNome;
    private String criadoPorEmail;
    private String alteradoPorNome;
    private String alteradoPorEmail;
}
