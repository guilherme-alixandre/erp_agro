package br.com.gado.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Espelha EMedicaoMeta, mas para o litros VENDIDOS de um lote dentro de uma meta de LEITE —
 * alimenta a barra "Vendido" ao lado de "Produzido" em MetaCard. Criado por
 * SDocumentoSaida.cadastrarVendaLeite quando existe uma EMetaSetor de LEITE ativa para o setor
 * onde o lote está alocado, cobrindo a data da venda.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "venda_meta_lote")
@Data
public class EVendaMetaLote extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "meta_setor_id", nullable = false)
    @JsonIgnore
    private EMetaSetor metaSetor;

    @ManyToOne
    @JoinColumn(name = "lote_id", nullable = false)
    private ELote lote;

    @Column(name = "data_venda", nullable = false)
    private LocalDate dataVenda;

    @Column(name = "litros_vendidos", nullable = false)
    private Double litrosVendidos;

    @Column(name = "criado_por_email")
    private String criadoPorEmail;
}
