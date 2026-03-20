package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum EnTipoMovimentacaoEstoque {
    ENTRADA("Entrada"),
    SAIDA("Saida"),
    APLICACAO("Aplicação"),
    PERDA("Perda");

    private final String EnTipoMovimentacaoEstoque;

    EnTipoMovimentacaoEstoque(String EnTipoMovimentacaoEstoque){
        this.EnTipoMovimentacaoEstoque = EnTipoMovimentacaoEstoque;
    }

}
