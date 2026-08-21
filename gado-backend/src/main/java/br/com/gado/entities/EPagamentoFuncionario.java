package br.com.gado.entities;

import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnStatusDespesa;
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
import java.time.LocalDate;

/** Histórico de um pagamento de folha para um funcionário em um Bloco Ano/Mês. */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "pagamento_funcionario")
@Data
public class EPagamentoFuncionario extends EAbstract {

    @ManyToOne
    @JoinColumn(name = "funcionario_id", nullable = false)
    private EFuncionario funcionario;

    @Column(name = "ano_referencia", nullable = false)
    private Integer anoReferencia;

    @Column(name = "mes_referencia", nullable = false)
    private Integer mesReferencia;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento", nullable = false, length = 20)
    private EnStatusDespesa statusPagamento;

    @Column(name = "valor_bruto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorBruto;

    @Column(name = "desconto_inss", nullable = false, precision = 15, scale = 2)
    private BigDecimal descontoInss;

    @Column(name = "desconto_outros", precision = 15, scale = 2)
    private BigDecimal descontoOutros;

    /** Custo da empresa (não desconta do funcionário) — recolhimento de FGTS sobre o salário do mês. */
    @Column(name = "encargo_fgts", nullable = false, precision = 15, scale = 2)
    private BigDecimal encargoFgts;

    /** Soma de VT + VA + plano de saúde concedidos no mês — também custo da empresa. */
    @Column(name = "valor_beneficios", precision = 15, scale = 2)
    private BigDecimal valorBeneficios;

    @Column(name = "valor_liquido", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorLiquido;

    /**
     * Cópia da natureza financeira do funcionário no momento do pagamento — preserva o
     * histórico do DRE mesmo que o funcionário mude de função/setor depois.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "natureza_financeira_snapshot", nullable = false, length = 20)
    private EnNaturezaFinanceira naturezaFinanceiraSnapshot;
}
