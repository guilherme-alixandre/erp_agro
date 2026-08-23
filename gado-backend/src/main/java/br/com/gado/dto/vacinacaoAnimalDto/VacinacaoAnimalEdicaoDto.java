package br.com.gado.dto.vacinacaoAnimalDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VacinacaoAnimalEdicaoDto {

    @NotNull(message = "Informe a dose por animal.")
    @Positive(message = "A dose por animal deve ser maior que zero.")
    private Double quantidadePorAnimal;

    private Long unidadeMedidaId;

    /** Se não informada, o Service usa LocalDateTime.now(). */
    private LocalDateTime dataAplicacao;
}
