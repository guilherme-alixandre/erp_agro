package br.com.gado.dto.resumoDto;

import lombok.Data;

@Data
public class AlertaEstoqueResumoDto {
    private Long insumoId;
    private String insumoNome;
    private Double saldoAtual;
    private Double estoqueMinimo;
    private String unidadeMedidaSigla;
}
