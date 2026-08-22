package br.com.gado.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "venda_leite_item")
@Data
public class EVendaLeiteItem extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "documento_saida_id", nullable = false)
    @JsonIgnore
    private EDocumentoSaida documentoSaida;

    @ManyToOne
    @JoinColumn(name = "lote_id", nullable = false)
    private ELote lote;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal litros;

    @Column(name = "preco_litro", nullable = false, precision = 15, scale = 4)
    private BigDecimal precoLitro;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;
}
