package br.com.gado.dto.vacinacaoAnimalDto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VacinacaoAnimalCadastroDto {

    @NotNull(message = "Selecione o produto (vacina) aplicado.")
    private Long insumoId;

    @NotNull(message = "Informe a dose por animal.")
    @Positive(message = "A dose por animal deve ser maior que zero.")
    private Double quantidadePorAnimal;

    private Long unidadeMedidaId;

    /** Se não informada, o Service usa LocalDateTime.now(). */
    private LocalDateTime dataAplicacao;

    /** Informativo — preenchido quando a origem da seleção foi "lote inteiro". */
    private Long loteId;

    @NotEmpty(message = "Selecione ao menos um animal.")
    private List<Long> animalIds;
}
