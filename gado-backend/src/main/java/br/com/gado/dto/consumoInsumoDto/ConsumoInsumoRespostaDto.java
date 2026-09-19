package br.com.gado.dto.consumoInsumoDto;

import br.com.gado.enums.EnStatusSobra;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsumoInsumoRespostaDto {
    private Long id;

    private Long insumoId;
    private String insumoNome;

    private Long setorId;
    private String setorNome;

    private Double quantidadeRegistrada;
    private String unidadeRegistroSigla;

    private Double quantidadeBaixaUnidadePrimaria;
    private String unidadeMedidaPrimariaSigla;

    private Integer totalAnimaisSetor;
    private Double consumoPorAnimal;

    private Double saldoAtualAposConsumo;

    private LocalDateTime dataConsumo;
    private String registradoPorEmail;
    private String registradoPorNome;

    private Boolean sobraRegistrada;
    private Double percentualSobra;
    private Boolean reaproveitado;
    private EnStatusSobra statusFaixa;
    private String mensagemRecomendacao;
}
