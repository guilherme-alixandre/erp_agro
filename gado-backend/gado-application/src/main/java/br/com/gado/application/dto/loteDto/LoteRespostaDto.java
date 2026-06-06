package br.com.gado.application.dto.loteDto;

import br.com.gado.application.dto.AbstractDTO;
import br.com.gado.domain.enums.EnStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoteRespostaDto extends AbstractDTO {

    private String codigo;
    private String descricao;
    private String racaPredominante;
    private String corBrinco;
    private LocalDate dataCriacao;
    private EnStatus statusLote;

    private String criadoPorNome;
    private String criadoPorEmail;

    private String alteradoPorNome;
    private String alteradoPorEmail;

    private List<LoteSetorRespostaDto> alocacoes;

    /** Total de animais somando todas as alocações. */
    private int totalAnimais;
}
