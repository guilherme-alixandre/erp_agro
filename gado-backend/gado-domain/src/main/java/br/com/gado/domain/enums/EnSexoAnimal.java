package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum EnSexoAnimal {
    M('M'),
    F('F');

    private final char sigla;

    EnSexoAnimal(char sigla){
        this.sigla = sigla;
    }

}
