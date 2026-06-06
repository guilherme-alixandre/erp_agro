package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnTipoSetor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "setor")
@Data
public class ESetor extends EAbstract{

    private String nome;
    private int capacidadeMaxima;
    private String metaTexto;
    private Double metaProducaoLeite;
    private Double metaArrobaAbate;

    @Enumerated(EnumType.STRING)
    private EnTipoSetor tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EUsuario criadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alterado_por_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EUsuario alteradoPor;

    @OneToMany(mappedBy = "setor")
    private List<ELoteSetor> lotes = new ArrayList<>();
}

