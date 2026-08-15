package br.com.gado.dto.loteDto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

/**
 * Permite atualizar corBrinco/descricao/racaPredominante e redistribuir alocações.
 * O código e o criadoPor são imutáveis após a criação.
 * Campos nulos são ignorados (atualização parcial).
 */
@Data
public class LotePutDto {

    private String corBrinco;
    private String descricao;
    private String racaPredominante;

    /**
     * Se fornecida (não nula), substitui completamente a lista de alocações atual.
     * Se nula, as alocações existentes são mantidas.
     */
    @Valid
    private List<LoteSetorCadastroDto> alocacoes;
}
