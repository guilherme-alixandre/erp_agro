package br.com.gado.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "lote")
@Data
public class ELote extends EAbstract{

    @Column(unique = true, nullable = false, length = 10)
    private String codigo;

    private String descricao;
    private String racaPredominante;
    private String corBrinco;

    @Column(nullable = false)
    private LocalDate dataCriacao;

    @ManyToOne
    @JoinColumn(name = "criado_por_id")
    @JsonIgnore
    private EUsuario criadoPor;

    @ManyToOne
    @JoinColumn(name = "alterado_por_id")
    @JsonIgnore
    private EUsuario alteradoPor;

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ELoteSetor> alocacoes = new ArrayList<>();

    @OneToMany(mappedBy = "lote")
    @JsonIgnore
    private List<ETransacao> transacoes = new ArrayList<>();
}
