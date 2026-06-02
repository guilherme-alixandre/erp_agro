package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnTipoMovimentacaoEstoque;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "movimentacao_estoque")
@Data
public class EMovimentacaoEstoque extends EAbstract{

    @Enumerated(EnumType.STRING)
    private EnTipoMovimentacaoEstoque EnTipoMovimentacaoEstoque;
    private double quantidade;
    private double valorUnitario;
    private Date dataMovimentacao;

    @ManyToOne
    @JoinColumn(name = "lote_id")
    private ELote loteId;

    @ManyToOne
    @JoinColumn(name = "setor_id_id")
    private ESetor setorId;

    @ManyToOne
    @JoinColumn(name = "parceiro_id_id")
    private EParceiro parceiroId;

    @ManyToOne
    @JoinColumn(name = "insumo_id_id")
    private EInsumo insumoId;

    @ManyToOne
    @JoinColumn(name = "animal_id_id")
    private EAnimal animalId;
}
