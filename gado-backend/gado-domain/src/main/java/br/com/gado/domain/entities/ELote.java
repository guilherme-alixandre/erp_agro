package br.com.gado.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "lote")
@Data
public class ELote extends EAbstract {

    /**
     * Código de negócio único auto-incrementável: LOT001, LOT002…
     * Gerado pelo SLote no momento do cadastro — NÃO é a PK do banco.
     */
    @Column(name = "codigo", unique = true, nullable = false, length = 6)
    private String codigo;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "raca_predominante")
    private String racaPredominante;

    @Column(name = "cor_brinco")
    private String corBrinco;

    @Column(name = "data_criacao", nullable = false)
    private LocalDate dataCriacao;

    @Column(name = "padrao", nullable = false)
    private boolean padrao = false;

    /**
     * Usuário que criou o lote.
     * Preenchido automaticamente pelo Service com base no email do header.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EUsuario criadoPor;

    /**
     * Usuário responsável pela última edição.
     * Nulo até a primeira alteração; atualizado pelo Service em cada PUT.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alterado_por_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EUsuario alteradoPor;

    /**
     * Alocações deste lote nos setores físicos.
     * Cada ELoteSetor liga um lote a um setor e guarda os animais alocados naquela divisão.
     */
    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<ELoteSetor> alocacoes = new ArrayList<>();

    /**
     * Transações financeiras vinculadas ao lote.
     * Mantido para não quebrar o relacionamento já existente em ETransacao.
     */
    @OneToMany(mappedBy = "lote")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<ETransacao> transacoes = new ArrayList<>();
}
