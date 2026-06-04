package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum EnTipoGado {
    BOVINO_JOVEM_50("Bovino jovem (macho)", 0.50),
    NOVILHA_DESCARTE_47_5("Novilha / vaca de descarte", 0.475),
    CONFINAMENTO_56("Animal terminado em confinamento", 0.56);

    private final String descricao;

    /**
     * Taxa de rendimento de carcaça utilizada no cálculo de arrobas.
     * Exemplo: 0.50 = 50%
     */
    private final double taxaRendimento;

    EnTipoGado(String descricao, double taxaRendimento) {
        this.descricao = descricao;
        this.taxaRendimento = taxaRendimento;
    }
}
