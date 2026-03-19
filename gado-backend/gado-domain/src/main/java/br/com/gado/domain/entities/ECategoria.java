package br.com.gado.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class ECategoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @NotBlank(message = "A categoria deve ter um nome/sigla para indentificação.")
    @Size(min = 1, max = 30, message = "A categoria deve ter entre 2 e 30 caracteres.")
    @Column(columnDefinition = "TEXT")
    private String categoria;
}
