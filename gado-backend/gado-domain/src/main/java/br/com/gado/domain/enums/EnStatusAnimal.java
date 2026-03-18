package br.com.gado.domain.enums;

import lombok.Getter;

// apenas um exemplo pra não passar batido
// MUDAR COM EXTREMA URGÊNCIA
@Getter
public enum EStatusAnimal {
    EX1("12"),
    EX2("21");

    private final String status;

    EStatusAnimal(String status){
        this.status = status;
    }

}
