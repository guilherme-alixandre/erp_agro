package br.com.gado.dto;

import br.com.gado.domain.entities.*;
import br.com.gado.domain.enums.EnTipoMovimentacaoEstoque;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.util.Date;


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
