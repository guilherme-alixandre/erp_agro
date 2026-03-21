package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnTipoSetor;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ESetor extends EAbstract{

    private String nome;
    private int capacidadeMaxima;
    private String metaTexto; // não lembro o que isso faz, só copiei mesmo
    private Double metaProducaoLeite;
    private Double metaArrobaAbate;

    @Enumerated(EnumType.STRING)
    private EnTipoSetor setor;

}
