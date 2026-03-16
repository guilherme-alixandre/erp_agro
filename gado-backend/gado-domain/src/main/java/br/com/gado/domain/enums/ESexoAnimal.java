package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum ESexoAnimal {
    M('M'),
    F('F');

    private final char sigla;

    ESexoAnimal(char sigla){
        this.sigla = sigla;
    }

}
