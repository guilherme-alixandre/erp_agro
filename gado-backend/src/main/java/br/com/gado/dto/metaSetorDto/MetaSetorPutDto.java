package br.com.gado.dto.metaSetorDto;

import br.com.gado.enums.EnTipoGado;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

/**
 * Permite atualizar campos editáveis da MetaSetor.
 * Campos nulos são ignorados. O setorId e o tipoMeta não são alteráveis após a criação.
 */
@Data
public class MetaSetorPutDto {

    private LocalDate dataInicial;
    private LocalDate dataFinal;

    @Positive(message = "A quantidade esperada deve ser maior que zero.")
    private Double quantidadeEsperada;

    @Positive(message = "O preço médio deve ser maior que zero.")
    private Double precoMedio;

    private EnTipoGado tipoGado;
}
