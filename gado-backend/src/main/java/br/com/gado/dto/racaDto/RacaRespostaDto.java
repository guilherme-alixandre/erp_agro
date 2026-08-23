package br.com.gado.dto.racaDto;

import br.com.gado.enums.EnStatus;
import lombok.Data;

@Data
public class RacaRespostaDto {
    private Long id;
    private String nome;
    private String sigla;
    private EnStatus status;

    private Long produtoId;
    private String produtoNome;
    private String produtoCodigo;

    /** Informativo — saldo (nº de cabeças) e preço médio por cabeça do produto vinculado. */
    private Double saldoAtual;
    private Double precoCompraMedio;
}
