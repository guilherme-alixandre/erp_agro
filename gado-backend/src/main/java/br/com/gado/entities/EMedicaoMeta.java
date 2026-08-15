package br.com.gado.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "medicao_meta")
@Data
public class EMedicaoMeta extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "meta_setor_id", nullable = false)
    @JsonIgnore
    private EMetaSetor metaSetor;

    @ManyToOne
    @JoinColumn(name = "lote_id", nullable = false)
    private ELote lote;

    @Column(name = "data_medicao", nullable = false)
    private LocalDate dataMedicao;

    /**
     * Quantidade bruta lançada: Litros (LEITE) ou Peso Vivo em Kg (ARROBA).
     * Para ARROBA, o serviço converte Kg → arrobas usando a taxa do tipoGado da meta.
     */
    @Column(name = "quantidade_lancada", nullable = false)
    private Double quantidadeLancada;

    @Column(name = "criado_por_email")
    private String criadoPorEmail;
}
