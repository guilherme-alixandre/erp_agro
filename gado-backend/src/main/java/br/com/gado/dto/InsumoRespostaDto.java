package br.com.gado.dto;

import br.com.gado.enums.EnTipoInsumo;
import lombok.Data;

@Data
public class InsumoRespostaDto {
    private Long id;
    private String nome;
    private EnTipoInsumo tipo;
    private Boolean pendente;
}
