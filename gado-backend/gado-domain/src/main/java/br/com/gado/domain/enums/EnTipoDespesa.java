package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum EnTipoDespesa {
    RECEITA("Receita"),
    DESPESA("Despesa");

    private final String tipoDespesa;

    EnTipoDespesa(String tipoDespesa){
        this.tipoDespesa = tipoDespesa;
    }

}
