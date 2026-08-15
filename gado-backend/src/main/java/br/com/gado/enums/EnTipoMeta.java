package br.com.gado.enums;

import lombok.Getter;

@Getter
public enum EnTipoMeta {
    LEITE("Leite"),
    ARROBA("Arroba");

    private final String descricao;

    EnTipoMeta(String descricao) {
        this.descricao = descricao;
    }
}
