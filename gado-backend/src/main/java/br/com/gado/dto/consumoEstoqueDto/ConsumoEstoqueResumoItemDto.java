package br.com.gado.dto.consumoEstoqueDto;

import lombok.Data;

@Data
public class ConsumoEstoqueResumoItemDto {
    private Long insumoId;
    private String insumoNome;
    private Double quantidadeTotal;
    private String unidadeSigla;
}
