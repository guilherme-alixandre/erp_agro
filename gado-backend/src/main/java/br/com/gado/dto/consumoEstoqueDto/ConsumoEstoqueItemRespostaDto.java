package br.com.gado.dto.consumoEstoqueDto;

import lombok.Data;

@Data
public class ConsumoEstoqueItemRespostaDto {
    private Long id;

    private Long insumoId;
    private String insumoNome;

    private Double quantidadeRegistrada;
    private String unidadeRegistroSigla;

    private Double quantidadeBaixaUnidadePrimaria;
    private String unidadeMedidaPrimariaSigla;

    private Double saldoAtualAposConsumo;
}
