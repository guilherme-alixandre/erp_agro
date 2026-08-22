package br.com.gado.dto.consumoInsumoDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsumoInsumoEdicaoDto {

    @NotNull(message = "A quantidade consumida é obrigatória.")
    @Positive(message = "A quantidade consumida deve ser maior que zero.")
    private Double quantidade;

    /** Unidade em que 'quantidade' foi informada. Se não informada, assume-se a unidade primária. */
    private Long unidadeMedidaId;

    /** Se não informada, o Service usa LocalDateTime.now(). */
    private LocalDateTime dataConsumo;
}
