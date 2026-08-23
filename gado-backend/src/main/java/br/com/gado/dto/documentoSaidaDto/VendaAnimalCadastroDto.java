package br.com.gado.dto.documentoSaidaDto;

import br.com.gado.enums.EnStatusAnimal;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Venda ou abate de um ou mais animais. O frontend resolve "um único animal" ou "um lote inteiro"
 * para uma lista explícita de animalIds antes de enviar (mesma UX de alocação de animais em Lotes).
 * valorTotal é dividido igualmente entre os animais informados.
 */
@Data
public class VendaAnimalCadastroDto {

    @NotNull(message = "Informe a data da venda.")
    private LocalDate dataEmissao;

    private String numeroDocumento;
    private String chaveAcesso;

    @NotNull(message = "Informe o destino: VENDIDO (vivo) ou ABATIDO.")
    private EnStatusAnimal destino;

    @NotEmpty(message = "Selecione ao menos um animal.")
    private List<Long> animalIds;

    @NotNull(message = "Informe o valor total da venda.")
    private BigDecimal valorTotal;

    @NotNull(message = "Informe o comprador.")
    private Long compradorId;
}
