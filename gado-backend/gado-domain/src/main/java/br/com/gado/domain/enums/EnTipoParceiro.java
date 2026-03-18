package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum EnTipoParceiro {
    PRIMEIRO("Tipo 1"),
    SEGUNDO("Tipo 2");

    private final String tipo;

    EnTipoParceiro(String tipo){
        this.tipo = tipo;
    }

}
