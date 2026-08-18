package br.com.gado.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "grupo_produto")
@Data
public class EGrupoProduto extends EAbstract {

    @Column(nullable = false)
    private String nome;

    /** Prefixo de 2 dígitos usado na geração do código do produto (ex: "01", "02"). */
    @Column(name = "codigo_prefixo", nullable = false, length = 2, unique = true)
    private String codigoPrefixo;
}
