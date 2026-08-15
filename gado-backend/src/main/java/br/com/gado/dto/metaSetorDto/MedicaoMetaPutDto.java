package br.com.gado.dto.metaSetorDto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicaoMetaPutDto {

    private Long loteId;
    private LocalDate dataMedicao;

    @Positive(message = "A quantidade deve ser maior que zero.")
    private Double quantidadeLancada;
}
