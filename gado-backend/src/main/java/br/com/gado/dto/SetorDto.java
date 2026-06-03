package br.com.gado.dto;

import br.com.gado.enums.EnTipoSetor;
import lombok.Data;

@Data
public class SetorDto {
    private Long id; // para o front-end
    private String nome;
    private int capacidadeMaxima;
    private String metaTexto;
    private Double metaProducaoLeite;
    private Double metaArrobaAbate;
    private EnTipoSetor setor;

}
