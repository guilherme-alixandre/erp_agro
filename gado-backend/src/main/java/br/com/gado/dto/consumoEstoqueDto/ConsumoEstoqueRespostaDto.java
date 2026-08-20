package br.com.gado.dto.consumoEstoqueDto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConsumoEstoqueRespostaDto {
    private Long id;

    private String motivo;
    private LocalDateTime dataConsumo;
    private String criadoPorEmail;
    private String criadoPorNome;

    private Boolean cancelado;
    private String motivoCancelamento;
    private String canceladoPorEmail;
    private String canceladoPorNome;
    private LocalDateTime canceladoEm;

    private List<ConsumoEstoqueItemRespostaDto> itens;
}
