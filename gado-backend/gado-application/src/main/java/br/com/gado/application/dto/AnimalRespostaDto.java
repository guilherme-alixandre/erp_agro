package br.com.gado.application.dto;

import br.com.gado.domain.enums.EnSexoAnimal;
import br.com.gado.domain.enums.EnStatusAnimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AnimalRespostaDto extends AbstractDTO {
    private String codigoBrinco;
    private String nome;
    private LocalDateTime dataNascimento;
    private Double pesoAtual;
    private String raca;
    private String cor;
    private Double alturaCernelha;
    private Double perimetroToracico;
    private Double comprimentoCorporal;
    private EnSexoAnimal sexo;
    private EnStatusAnimal statusAnimal;
    private List<VacinacaoDTO> vacinas;
}
