package br.com.gado.entities;

import br.com.gado.enums.EnStatusAnimal;
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

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "venda_animal_item")
@Data
public class EVendaAnimalItem extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "documento_saida_id", nullable = false)
    @JsonIgnore
    private EDocumentoSaida documentoSaida;

    @ManyToOne
    @JoinColumn(name = "animal_id", nullable = false)
    private EAnimal animal;

    /** Produto (EInsumo) da raça do animal — recebe a baixa de 1 cabeça nesta venda. */
    @ManyToOne
    @JoinColumn(name = "produto_id")
    private EInsumo produto;

    /** VENDIDO (vivo) ou ABATIDO. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnStatusAnimal destino;

    @Column(name = "valor_venda", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorVenda;
}
