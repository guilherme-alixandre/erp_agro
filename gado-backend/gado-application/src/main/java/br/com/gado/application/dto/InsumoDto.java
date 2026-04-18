package br.com.gado.application.dto;

import br.com.gado.domain.entities.EAbstract;
import br.com.gado.domain.enums.EnTipoInsumo;
import lombok.Data;

@Data
public class InsumoDto extends AbstractDTO {

    private String nome;
    private Double estoqueMinimo;
    private Double saldoAtual;
    private Long parceiro_id;
    private EnTipoInsumo tipo;

}
