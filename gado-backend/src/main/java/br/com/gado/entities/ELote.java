package br.com.gado.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "lote")
@Data
public class ELote extends EAbstract{

    private String descricao;
    private String racaPredominante;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private EUsuario usuario;

    @OneToMany(mappedBy = "lote")
    private List<ETransacao> transacoes = new ArrayList<>();

    @ManyToOne
    private ESetor setor;

}
