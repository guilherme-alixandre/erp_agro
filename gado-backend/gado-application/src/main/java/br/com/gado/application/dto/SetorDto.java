package br.com.gado.application.dto;

import br.com.gado.domain.enums.EnTipoSetor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SetorDto extends AbstractDTO {
    private String nome;
    private int capacidadeMaxima;
    private String metaTexto;
    private Double metaProducaoLeite;
    private Double metaArrobaAbate;
    private EnTipoSetor tipo;

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
