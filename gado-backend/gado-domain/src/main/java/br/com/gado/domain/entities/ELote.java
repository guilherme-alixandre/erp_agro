package br.com.gado.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class EntityLote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;
    private String racaPredominante;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private EntityUsuario usuario;

    @OneToMany(mappedBy = "lote")
    private List<EntityTransacao> transacoes = new ArrayList<>();

}
