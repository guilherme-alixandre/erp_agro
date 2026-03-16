package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum ETipoInsumo {
    PRIMEIRO("Tipo 1"),
    SEGUNDO("Tipo 2");

    private final String tipo;

    ETipoInsumo(String tipo){
        this.tipo = tipo;
    }

}
