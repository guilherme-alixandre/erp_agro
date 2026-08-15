package br.com.gado.dto.metaSetorDto;

import br.com.gado.enums.EnTipoGado;
import br.com.gado.enums.EnTipoMeta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MetaSetorCadastroDto {

    @NotNull(message = "O ID do setor é obrigatório.")
    private Long setorId;

    @NotNull(message = "A data inicial é obrigatória.")
    private LocalDate dataInicial;

    @NotNull(message = "A data final é obrigatória.")
    private LocalDate dataFinal;

    @NotNull(message = "O tipo de meta é obrigatório (LEITE ou ARROBA).")
    private EnTipoMeta tipoMeta;

    @NotNull(message = "A quantidade esperada é obrigatória.")
    @Positive(message = "A quantidade esperada deve ser maior que zero.")
    private Double quantidadeEsperada;

    @NotNull(message = "O preço médio é obrigatório.")
    @Positive(message = "O preço médio deve ser maior que zero.")
    private Double precoMedio;

    /** Obrigatório quando tipoMeta == ARROBA. A validação de negócio é feita no Service. */
    private EnTipoGado tipoGado;
}
