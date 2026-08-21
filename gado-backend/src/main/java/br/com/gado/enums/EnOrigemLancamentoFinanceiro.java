package br.com.gado.enums;

import lombok.Getter;

/** De onde um ELancamentoFinanceiro (linha do razão/DRE) foi gerado. */
@Getter
public enum EnOrigemLancamentoFinanceiro {
    DOCUMENTO_ENTRADA,
    FOLHA_PAGAMENTO,
    CONSUMO_ESTOQUE,
    VENDA,
    MANUAL
}
