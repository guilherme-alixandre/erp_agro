package br.com.gado.application.dto.loteDto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

/**
 * Permite atualizar corBrinco e redistribuir alocações.
 * O código (LOTxxx) e o criadoPor são imutáveis após a criação.
 * Campos nulos são ignorados (atualização parcial).
 */
@Data
public class LotePutDto {

    private String corBrinco;

    private String descricao;

    private String racaPredominante;

    /**
     * Se fornecida, substitui completamente a lista de alocações atual.
     * Se nula, as alocações existentes são mantidas.
     */
    @Valid
    private List<LoteSetorCadastroDto> alocacoes;
}
