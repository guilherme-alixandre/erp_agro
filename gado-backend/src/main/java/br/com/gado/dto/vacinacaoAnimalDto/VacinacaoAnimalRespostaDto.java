package br.com.gado.dto.vacinacaoAnimalDto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VacinacaoAnimalRespostaDto {

    private Long id;

    private Long insumoId;
    private String insumoNome;

    private Double quantidadePorAnimal;
    private String unidadeRegistroSigla;

    private Double quantidadeBaixaPorAnimalUnidadePrimaria;
    private String unidadeMedidaPrimariaSigla;
    private Double quantidadeTotalBaixaUnidadePrimaria;
    private Double saldoAtualAposAplicacao;

    private Integer totalAnimais;
    private List<AnimalResumoItemDto> animais;

    private Long loteId;
    private String loteCodigo;

    private LocalDateTime dataAplicacao;
    private String criadoPorEmail;
    private String criadoPorNome;

    private Boolean cancelado;
    private String motivoCancelamento;
    private String canceladoPorEmail;
    private String canceladoPorNome;
    private LocalDateTime canceladoEm;
}
