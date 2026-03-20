package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum EnTipoOcorrencia {

    NASCIMENTO("Nascimento"),
    OBITO("Obito"),
    DOENCA("Doença"),
    VACINACAO("Vacinação"),
    PESAGEM("Pesagem");

    private final String ocorrencia;

    EnTipoOcorrencia(String ocorrencia) {
        this.ocorrencia = ocorrencia;
    }
}
