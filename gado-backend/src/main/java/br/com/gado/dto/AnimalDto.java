package br.com.gado.dto;

import br.com.gado.enums.EnSexoAnimal;
import br.com.gado.enums.EnStatusAnimal;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class AnimalDto extends AbstractDTO {

    /** Gerado automaticamente (sigla da raça + sequencial) — ignorado se enviado pelo cliente. */
    private String codigoBrinco;
    private String cor;
    private LocalDateTime dataNascimento;

    @DecimalMin(value = "30.0", message = "O peso mínimo para cadastro é 30kg.")
    private Double pesoAtual;

    /** Opcional: preço e data de compra do animal, usados no custo de vida (ver SCustoAnimal). */
    private BigDecimal valorAquisicao;
    private LocalDateTime dataAquisicao;

    /** Obrigatório na criação. */
    private Long racaId;
    private String racaNome;
    private String racaSigla;

    private Double alturaCernelha;
    private Double perimetroToracico;
    private Double comprimentoCorporal;
    private EnSexoAnimal sexo;
    private EnStatusAnimal statusAnimal;
}
