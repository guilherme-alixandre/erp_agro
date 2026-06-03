package br.com.gado.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CategoriaDTO extends AbstractDTO {
    private String categoria;
}
