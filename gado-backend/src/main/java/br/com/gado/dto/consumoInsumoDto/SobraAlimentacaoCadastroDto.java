package br.com.gado.dto.consumoInsumoDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class SobraAlimentacaoCadastroDto {

    @NotNull(message = "A quantidade de sobra é obrigatória.")
    @PositiveOrZero(message = "A quantidade de sobra não pode ser negativa.")
    private Double quantidade;

    /**
     * Unidade em que 'quantidade' foi informada. Deve ser a unidade primária ou a
     * secundária cadastrada no insumo. Se não informada, assume-se a unidade primária.
     */
    private Long unidadeMedidaId;

    @NotNull(message = "Informe se a sobra foi reaproveitada.")
    private Boolean reaproveitado;
}
