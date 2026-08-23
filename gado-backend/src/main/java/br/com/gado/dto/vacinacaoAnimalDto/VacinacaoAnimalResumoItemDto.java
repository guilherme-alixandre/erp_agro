package br.com.gado.dto.vacinacaoAnimalDto;

import lombok.Data;

@Data
public class VacinacaoAnimalResumoItemDto {
    private Long insumoId;
    private String insumoNome;
    private Double quantidadeTotal;
    private String unidadeSigla;
}
