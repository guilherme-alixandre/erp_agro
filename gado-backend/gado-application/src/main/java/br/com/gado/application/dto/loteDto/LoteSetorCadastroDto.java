package br.com.gado.application.dto.loteDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa a alocação de um lote em um setor específico.
 * Enviado dentro de {@link LoteCadastroDto#getAlocacoes()}.
 */
@Data
public class LoteSetorCadastroDto {

    @NotNull(message = "O ID do setor é obrigatório em cada alocação.")
    private Long setorId;

    /**
     * IDs dos animais a alocar neste setor.
     * Pode ser vazio — o lote é criado no setor mesmo sem animais ainda atribuídos.
     */
    private List<Long> animaisIds = new ArrayList<>();
}
