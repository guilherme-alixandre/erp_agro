package br.com.gado.dto.consumoEstoqueDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ConsumoEstoqueItemCadastroDto {

    @NotNull(message = "O produto é obrigatório.")
    private Long insumoId;

    @NotNull(message = "A quantidade consumida é obrigatória.")
    @Positive(message = "A quantidade consumida deve ser maior que zero.")
    private Double quantidade;

    /**
     * Unidade em que 'quantidade' foi informada. Deve ser a unidade primária ou a
     * secundária cadastrada no insumo. Se não informada, assume-se a unidade primária.
     */
    private Long unidadeMedidaId;
}
