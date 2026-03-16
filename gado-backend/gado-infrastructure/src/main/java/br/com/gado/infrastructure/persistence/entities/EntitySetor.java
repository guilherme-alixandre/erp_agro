package br.com.gado.infrastructure.persistence.entities;

import br.com.gado.domain.enums.ETipoSetor;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class EntitySetor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private int capacidadeMaxima;
    private String metaTexto; // não lembro o que isso faz, só copiei mesmo
    private Double metaProducaoLeite;
    private Double metaArrobaAbate;

    @Enumerated(EnumType.STRING)
    private ETipoSetor setor;

}
