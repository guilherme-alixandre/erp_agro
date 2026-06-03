package br.com.gado.entities;

import br.com.gado.enums.EnTipoSetor;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    @OneToMany(mappedBy = "setor")
    private List<ELote> lotes = new ArrayList<>();
}

