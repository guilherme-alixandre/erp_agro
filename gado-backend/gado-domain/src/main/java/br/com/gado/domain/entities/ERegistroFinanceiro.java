package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnStatusDespesa;
import br.com.gado.domain.enums.EnTipoDespesa;
import jakarta.persistence.*;

import java.util.Date;

@Entity
public class ERegistroFinanceiro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private ECategoria categoria_id;

    @ManyToOne
    @JoinColumn(name = "pessoa_id")
    private EUsuario usuario_id;
}
