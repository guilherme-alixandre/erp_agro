package br.com.gado.dto.metaSetorDto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicaoMetaRespostaDto {

    private Long id;
    private Long loteId;
    private String loteCodigo;
    private String loteDescricao;
    private LocalDate dataMedicao;

    /** Quantidade bruta lançada (Litros ou Peso Vivo em Kg). */
    private Double quantidadeLancada;

    /** Quantidade já convertida para a unidade da meta (Litros ou Arrobas). */
    private Double quantidadeConvertida;

    private String criadoPorEmail;
    private String criadoPorNome;
    private String criadoPorPerfil;
}
