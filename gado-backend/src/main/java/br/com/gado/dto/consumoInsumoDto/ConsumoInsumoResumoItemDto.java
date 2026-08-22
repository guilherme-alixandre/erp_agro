package br.com.gado.dto.consumoInsumoDto;

import lombok.Data;

@Data
public class ConsumoInsumoResumoItemDto {
    private Long insumoId;
    private String insumoNome;
    private Double quantidadeTotal;
    private String unidadeSigla;
}
