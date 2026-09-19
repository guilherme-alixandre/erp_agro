package br.com.gado.entities;

import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnTipoInsumo;
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

    /** Usado para determinar o tipo de insumo ao qual o grupo vai estar atrelado. Assim é possivel filtrar grupos em todo o sistema  */
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_grupo", nullable = false, length = 20)
    private EnTipoInsumo categoriaGrupo;

    /** CUSTO: gera retorno financeiro em produtos vendidos. GASTO: não gera retorno direto. */
    @Enumerated(EnumType.STRING)
    @Column(name = "natureza_financeira", nullable = false, length = 20)
    private EnNaturezaFinanceira naturezaFinanceira;
}
