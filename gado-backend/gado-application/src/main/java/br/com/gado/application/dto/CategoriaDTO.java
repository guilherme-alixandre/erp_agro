package br.com.gado.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CategoriaDTO extends AbstractDTO {
    private String categoria;
}
