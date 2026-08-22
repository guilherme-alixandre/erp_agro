package br.com.gado.dto.consumoEstoqueDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConsumoEstoqueEdicaoDto {

    @NotBlank(message = "Informe o motivo/finalidade da saída de estoque.")
    private String motivo;

    /** Se não informada, o Service usa LocalDateTime.now(). */
    private LocalDateTime dataConsumo;

    @NotEmpty(message = "Informe ao menos um produto consumido.")
    @Valid
    private List<ConsumoEstoqueItemCadastroDto> itens;
}
