package br.com.gado.application.dto.loteDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class LoteCadastroDto {

    @NotBlank(message = "A cor do brinco é obrigatória.")
    private String corBrinco;

    private String descricao;

    private String racaPredominante;

    /**
     * Se não informada, o Service usa LocalDate.now().
     */
    private LocalDate dataCriacao;

    /**
     * Lista de setores e os animais a alocar em cada um.
     * Pode ser vazia — o lote é cadastrado sem alocações iniciais.
     */
    @Valid
    private List<LoteSetorCadastroDto> alocacoes = new ArrayList<>();
}
