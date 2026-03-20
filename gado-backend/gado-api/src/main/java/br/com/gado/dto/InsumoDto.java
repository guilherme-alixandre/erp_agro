package br.com.gado.dto;

import br.com.gado.domain.enums.EnTipoInsumo;
import lombok.Data;

@Data
public class InsumoDto {

    private String nome;
    private Double estoqueMinimo;
    private Double saldoAtual;
    private Long parceiro_id;
    private EnTipoInsumo tipo;

}
