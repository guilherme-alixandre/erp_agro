package br.com.gado.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class EUnidadeMedida extends EAbstract{

    @NotBlank(message = "A unidade deve ter um nome/sigla para indentificação.")
    @Size(min = 1, max = 30, message = "A Unidade deve ter entre 2 e 30 caracteres.")
    @Column(columnDefinition = "TEXT")
    private String unidade;
}
