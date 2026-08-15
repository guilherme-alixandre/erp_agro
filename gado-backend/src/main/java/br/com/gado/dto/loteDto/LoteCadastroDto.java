package br.com.gado.dto.loteDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class LoteCadastroDto {

    @NotBlank(message = "A cor do brinco é obrigatória.")
    private String corBrinco;

    private String descricao;
    private String racaPredominante;

    /** Se não informada, o Service usa LocalDate.now(). */
    private LocalDate dataCriacao;

    @NotEmpty(message = "O lote deve ser alocado em pelo menos um setor.")
    @Valid
    private List<LoteSetorCadastroDto> alocacoes;
}
