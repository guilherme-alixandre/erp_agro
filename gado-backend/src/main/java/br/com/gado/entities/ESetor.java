package br.com.gado.entities;

import br.com.gado.enums.EnTipoSetor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    @ManyToOne
    @JoinColumn(name = "criado_por_id")
    @JsonIgnore
    private EUsuario criadoPor;

    @ManyToOne
    @JoinColumn(name = "alterado_por_id")
    @JsonIgnore
    private EUsuario alteradoPor;
}

