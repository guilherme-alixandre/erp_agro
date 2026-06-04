package br.com.gado.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "medicao_meta")
@Data
public class EMedicaoMeta extends EAbstract {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meta_setor_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EMetaSetor metaSetor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private ELote lote;

    @Column(name = "data_medicao", nullable = false)
    private LocalDate dataMedicao;

    /**
     * Quantidade lançada: Litros (LEITE) ou Peso Vivo em Kg (ARROBA).
     * Para ARROBA, o serviço converte Kg → arrobas usando a taxa do tipoGado da meta.
     */
    @Column(name = "quantidade_lancada", nullable = false)
    private Double quantidadeLancada;
}
