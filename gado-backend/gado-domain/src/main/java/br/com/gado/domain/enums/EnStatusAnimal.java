package br.com.gado.domain.enums;

import lombok.Getter;

// apenas um exemplo pra não passar batido
// MUDAR COM EXTREMA URGÊNCIA
@Getter
public enum EnStatusAnimal {
    EX1("12"),
    EX2("21");

    private final String status;

    EnStatusAnimal(String status){
        this.status = status;
    }

}
