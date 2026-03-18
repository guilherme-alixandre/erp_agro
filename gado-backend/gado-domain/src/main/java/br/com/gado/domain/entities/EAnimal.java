package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnSexoAnimal;
import br.com.gado.domain.enums.EnStatusAnimal;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
public class EAnimal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoBrinco;
    private String nome;
    private LocalDateTime dataNascimento;
    private Double pesoAtual;
    private String raca;
    private String cor;
    private String tamanho;

    @Enumerated(EnumType.STRING)
    private EnSexoAnimal sexo;

    @Enumerated(EnumType.STRING)
    private EnStatusAnimal status;

    // no banco vai ficar o "pessoa_id"
    // por estar carregando um entity, a gente chama ele com esse nome horrível por agora
    @ManyToOne
    @JoinColumn(name = "pessoa_id")
    private EUsuario usuario_id;

}
