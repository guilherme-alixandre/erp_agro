package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum ETipoSetor {
    TIPO1("Tipo 1"),
    TIPO2("Tipo 2");

    private final String tipo;

    ETipoSetor(String tipo){
        this.tipo = tipo;
    }
}
