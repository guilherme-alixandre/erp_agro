package br.com.gado.domain.enums;

import lombok.Getter;

// apenas um exemplo pra não passar batido
// MUDAR COM EXTREMA URGÊNCIA
@Getter
public enum EnStatusDespesa {
    ATRASADO("Atrasado"),
    PAGO("Pago"),
    PENDENTE("Pendente");

    private final String statusDespesa;

    EnStatusDespesa(String statusDespesa){
        this.statusDespesa = statusDespesa;
    }

}
