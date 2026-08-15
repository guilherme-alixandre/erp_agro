package br.com.gado.dto.metaSetorDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicaoMetaCadastroDto {

    @NotNull(message = "O ID da meta do setor é obrigatório.")
    private Long metaSetorId;

    @NotNull(message = "O ID do lote é obrigatório.")
    private Long loteId;

    @NotNull(message = "A data da medição é obrigatória.")
    private LocalDate dataMedicao;

    @NotNull(message = "A quantidade lançada é obrigatória.")
    @Positive(message = "A quantidade lançada deve ser maior que zero.")
    private Double quantidadeLancada;
}
