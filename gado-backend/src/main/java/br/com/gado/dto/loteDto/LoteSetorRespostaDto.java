package br.com.gado.dto.loteDto;

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

    private List<AnimalResumoDto> animais;

    @Data
    public static class AnimalResumoDto {
        private Long id;
        private String codigoBrinco;
        private String racaNome;
    }
}
