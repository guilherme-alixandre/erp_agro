package br.com.gado.application.dto;

import br.com.gado.domain.enums.EnTipoInsumo;
import lombok.Data;

@Data
public class InsumoRespostaDto {
    private Long id;
    private String nome;
    private EnTipoInsumo tipo;
    private Boolean pendente;
}
