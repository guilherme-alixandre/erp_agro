package br.com.gado.dto;

import br.com.gado.entities.EAbstract;
import br.com.gado.enums.EnTipoInsumo;
import lombok.Data;

@Data
public class InsumoDto extends AbstractDTO {

    private String nome;
    private Double estoqueMinimo;
    private Double saldoAtual;
    private Long parceiro_id;
    private EnTipoInsumo tipo;
    private Boolean pendente;

}
