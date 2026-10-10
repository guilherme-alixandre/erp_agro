package br.com.gado.entities;

import br.com.gado.enums.EnTipoCustoAnimal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ledger histórico de custo de um animal (ver SCustoAnimal). Diferente do custo de ração por lote
 * (CustoRacaoLoteDto), aqui o preço é fotografado no momento de cada evento e o registro pertence
 * ao animal — por isso o custo "viaja" com ele quando troca de lote.
 *
 * origemTipo/origemId identificam o registro de origem (EConsumoInsumo, EVacinacaoAnimalItem, ou
 * o próprio EAnimal no caso de aquisição) para permitir estorno/recálculo idempotente quando a
 * origem é editada ou cancelada.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "custo_animal")
@Data
public class ECustoAnimal extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "animal_id", nullable = false)
    @JsonIgnore
    private EAnimal animal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_custo", nullable = false)
    private EnTipoCustoAnimal tipoCusto;

    @Column(nullable = false)
    private BigDecimal valor;

    /** Quantidade consumida/aplicada que originou o valor (ração, dose) — informativo. */
    private Double quantidade;

    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;

    @Column(name = "origem_tipo")
    private String origemTipo;

    @Column(name = "origem_id")
    private Long origemId;

    private String descricao;

    @Column(name = "registrado_por_email")
    private String registradoPorEmail;
}
