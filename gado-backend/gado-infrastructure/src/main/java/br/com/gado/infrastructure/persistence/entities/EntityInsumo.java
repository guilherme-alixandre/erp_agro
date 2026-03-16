package br.com.gado.infrastructure.persistence.entities;

import br.com.gado.domain.enums.ETipoInsumo;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class EntityInsumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Double estoqueMinimo;
    private Double saldoAtual;

    @ManyToOne
    @JoinColumn(name = "parceiro_id")
    private EntityParceiro parceiro;

    @Enumerated(EnumType.STRING)
    private ETipoInsumo tipo;

    // fazer depois o unidade_medida pq não lembro pra que isso

}
