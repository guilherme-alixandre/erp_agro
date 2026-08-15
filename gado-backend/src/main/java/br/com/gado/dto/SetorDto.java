package br.com.gado.dto;

import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoSetor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SetorDto {
    private Long id;
    private String nome;
    private int capacidadeMaxima;
    private String metaTexto;
    private Double metaProducaoLeite;
    private Double metaArrobaAbate;
    private EnTipoSetor tipo;
    private EnStatus status;

    private String criadoPorNome;
    private String criadoPorEmail;
    private String alteradoPorNome;
    private String alteradoPorEmail;

    private List<LoteResumoDto> lotes = new ArrayList<>();

    @Data
    public static class LoteResumoDto {
        private Long loteSectorId;
        private Long loteId;
        private String loteCodigo;
        private String loteCorBrinco;
        private int quantidadeAnimais;
    }
}
