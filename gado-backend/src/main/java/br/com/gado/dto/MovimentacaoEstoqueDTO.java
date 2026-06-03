package br.com.gado.dto;

import br.com.gado.entities.*;
import br.com.gado.enums.EnTipoMovimentacaoEstoque;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;


@EqualsAndHashCode(callSuper = true)
@Data
public class MovimentacaoEstoqueDTO extends AbstractDTO {

    private EnTipoMovimentacaoEstoque EnTipoMovimentacaoEstoque;
    private double quantidade;
    private double valorUnitario;
    private Date dataMovimentacao;
    private ELote loteId;
    private ESetor setorId;
    private EParceiro parceiroId;
    private EInsumo insumoId;
    private EAnimal animalId;

}
