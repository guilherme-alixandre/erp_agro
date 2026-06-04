package br.com.gado.entities;

import br.com.gado.enums.EnStatusDespesa;
import br.com.gado.enums.EnTipoDespesa;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "registro_financeiro")
@Data
public class ERegistroFinanceiro extends EAbstract{

    private String descricao;

    @Enumerated(EnumType.STRING)
    private EnTipoDespesa tipoDespesa;

    private Double valor;

    private Date dataVencimento;
    private Date dataPagamento;

    @Enumerated(EnumType.STRING)
    private EnStatusDespesa statusDespesa;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private ECategoria categoriaId;

    @ManyToOne
    @JoinColumn(name = "pessoa_id")
    private EUsuario usuarioId;
}
