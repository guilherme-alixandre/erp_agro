package br.com.gado.enums;

import lombok.Getter;

// Vamos chamar de "Categoria de grupo" para os grupos de produto
@Getter
public enum EnTipoInsumo {
    RACAO,
    VACINA,
    MEDICAMENTO,
    OUTROS,
    ANIMAL
}
