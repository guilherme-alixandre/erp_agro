package br.com.gado.enums;

import lombok.Getter;

/** Motivo da exclusão/cancelamento de uma venda (EDocumentoSaida) já registrada. */
@Getter
public enum EnMotivoCancelamentoVenda {
    DEVOLVIDA,
    CANCELADA
}
