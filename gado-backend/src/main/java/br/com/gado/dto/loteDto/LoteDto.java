package br.com.gado.dto.loteDto;

import br.com.gado.enums.EnStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class LoteDto {
    private Long id;
    private String codigo;
    private String descricao;
    private String racaPredominante;
    private String corBrinco;
    private LocalDate dataCriacao;
    private EnStatus status;

    private String criadoPorNome;
    private String criadoPorEmail;
    private String alteradoPorNome;
    private String alteradoPorEmail;

    private List<LoteSetorRespostaDto> alocacoes = new ArrayList<>();
    private int totalAnimais;
}
