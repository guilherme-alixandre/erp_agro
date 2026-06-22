package br.com.gado.application.dto.loteDto;

import lombok.Data;

import java.util.List;

/**
 * Representa uma alocação (ELoteSetor) na resposta do lote.
 */
@Data
public class LoteSetorRespostaDto {

    private Long loteSectorId;
    private Long setorId;
    private String setorNome;
    private int capacidadeMaxima;

    /** IDs e nomes resumidos dos animais alocados neste setor. */
    private List<AnimalResumoDto> animais;

    @Data
    public static class AnimalResumoDto {
        private Long id;
        private String codigoBrinco;
        private String nome;
    }
}
