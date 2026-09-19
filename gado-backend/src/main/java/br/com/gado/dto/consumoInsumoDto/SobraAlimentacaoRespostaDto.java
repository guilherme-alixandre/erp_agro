package br.com.gado.dto.consumoInsumoDto;

import br.com.gado.enums.EnStatusSobra;
import lombok.Data;

@Data
public class SobraAlimentacaoRespostaDto {
    private Long consumoInsumoId;

    private Double quantidadeSobraRegistrada;
    private String unidadeRegistroSigla;

    private Double quantidadeSobraUnidadePrimaria;
    private String unidadeMedidaPrimariaSigla;

    private Double percentualSobra;
    private Boolean reaproveitado;
    private EnStatusSobra statusFaixa;

    /** Quantidade recomendada de ajuste (aumentar se ABAIXO, reduzir se ACIMA), na unidade primária. */
    private Double quantidadeAjusteRecomendada;

    /** Mensagem pronta (pt-BR) para exibir ao usuário. */
    private String mensagem;
}
