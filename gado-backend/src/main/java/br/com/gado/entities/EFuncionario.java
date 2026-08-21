package br.com.gado.entities;

import br.com.gado.enums.EnNaturezaFinanceira;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dados cadastrais e contratuais de um funcionário (campos padrão CLT/Rural). A folha em si
 * (histórico de pagamentos) fica em EPagamentoFuncionario. naturezaFinanceira classifica se o
 * custo desse funcionário é produtivo (CUSTO, ex: cuidador) ou administrativo (GASTO, ex: financeiro).
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "funcionario")
@Data
public class EFuncionario extends EAbstract {

    /** Vínculo opcional com um login do sistema — nem todo funcionário (ex: cuidador de campo) precisa acessar o ERP. */
    @OneToOne
    @JoinColumn(name = "usuario_id")
    private EUsuario usuario;

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false)
    private String cargo;

    @Column(name = "data_admissao", nullable = false)
    private LocalDate dataAdmissao;

    @Column(name = "data_demissao")
    private LocalDate dataDemissao;

    @Column(name = "salario_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal salarioBase;

    /** Percentual de desconto de INSS do empregado (tabela progressiva CLT/Rural), ex: 9.00 = 9%. */
    @Column(name = "percentual_inss", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualInss;

    /** Percentual de FGTS recolhido pelo empregador (custo da empresa, não desconta do funcionário), padrão 8%. */
    @Column(name = "percentual_fgts", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualFgts;

    @Column(name = "valor_vale_transporte", precision = 15, scale = 2)
    private BigDecimal valorValeTransporte;

    @Column(name = "valor_vale_alimentacao", precision = 15, scale = 2)
    private BigDecimal valorValeAlimentacao;

    @Column(name = "valor_plano_saude", precision = 15, scale = 2)
    private BigDecimal valorPlanoSaude;

    /** CUSTO quando ligado diretamente à produção (ex: cuidador), GASTO quando administrativo (ex: financeiro). */
    @Enumerated(EnumType.STRING)
    @Column(name = "natureza_financeira", nullable = false, length = 20)
    private EnNaturezaFinanceira naturezaFinanceira;
}
