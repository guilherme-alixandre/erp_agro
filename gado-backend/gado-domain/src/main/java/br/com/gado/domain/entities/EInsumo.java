package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnTipoInsumo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "insumo")
@Data
public class EInsumo extends EAbstract{

    private String nome;
    private Double estoqueMinimo;
    private Double saldoAtual;

    @ManyToOne
    @JoinColumn(name = "parceiro_id")
    private EParceiro parceiro;

    @Enumerated(EnumType.STRING)
    private EnTipoInsumo tipo;

    // fazer depois o unidade_medida pq não lembro pra que isso

}
