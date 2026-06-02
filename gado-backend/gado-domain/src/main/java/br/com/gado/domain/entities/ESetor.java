package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnTipoSetor;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "setor")
@Data
public class ESetor extends EAbstract{

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private EUsuario usuario;

    private String descricao;
    private int capacidadeMaxima;
    private String metaTexto; // não lembro o que isso faz, só copiei mesmo
    private Double metaProducaoLeite;
    private Double metaArrobaAbate;

    @Enumerated(EnumType.STRING)
    private EnTipoSetor setor;

}
