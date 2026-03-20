package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum EnTipoInsumo {
    PRIMEIRO("Tipo 1"),
    SEGUNDO("Tipo 2");

    private final String tipo;

    EnTipoInsumo(String tipo){
        this.tipo = tipo;
    }

}
