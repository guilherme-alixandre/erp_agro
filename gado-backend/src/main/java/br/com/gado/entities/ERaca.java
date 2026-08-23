package br.com.gado.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Raça de animal. Ao ser criada, gera automaticamente um Produto (EInsumo) "Gado {nome}" no
 * Grupo de Produto "Animais" — esse produto é quem controla o saldo (nº de cabeças) e o preço
 * médio por cabeça, reaproveitando o mecanismo de entrada/saída de estoque já existente.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "raca")
@Data
public class ERaca extends EAbstract {

    @Column(nullable = false)
    private String nome;

    /** Sigla usada para montar o código do brinco dos animais desta raça (ex: "NE" → NE0001, NE0002...). */
    @Column(nullable = false, length = 4)
    private String sigla;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private EInsumo produto;
}
