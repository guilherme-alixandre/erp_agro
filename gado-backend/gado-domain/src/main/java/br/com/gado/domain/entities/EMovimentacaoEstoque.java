package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnTipoMovimentacaoEstoque;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class EMovimentacaoEstoque extends EAbstract{

    @Enumerated(EnumType.STRING)
    private EnTipoMovimentacaoEstoque EnTipoMovimentacaoEstoque;
    private double quantidade;
    private double valorUnitario;
    private Date dataMovimentacao;

    @ManyToOne
    @JoinColumns({
            // O 'name' é o nome da coluna na tabela movimentacao_estoque [cite: 84]
            // O 'referencedColumnName' é o nome da coluna na tabela lote
            @JoinColumn(name = "lote_id", referencedColumnName = "id"),

            // O 'name' é o nome da coluna na tabela movimentacao_estoque [cite: 85]
            // O 'referencedColumnName' é o nome da coluna na tabela lote (que é a FK de usuario)
            @JoinColumn(name = "lote_usuario_id", referencedColumnName = "usuario_id")
    })
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
