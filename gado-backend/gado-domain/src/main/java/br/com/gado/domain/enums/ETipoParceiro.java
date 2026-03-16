package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum ETipoParceiro {
    PRIMEIRO("Tipo 1"),
    SEGUNDO("Tipo 2");

    private final String tipo;

    ETipoParceiro(String tipo){
        this.tipo = tipo;
    }

}
