package br.com.gado.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ListasTarefasDTO extends AbstractDTO {

    private String nomeLista;
}
