package br.com.gado.dto.documentoEntradaDto;

import br.com.gado.enums.EnNaturezaFinanceira;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cadastro manual de um documento não fiscal (ex: recibo de prestador). Sempre nasce PENDENTE_APROVACAO. */
@Data
public class ReciboSimplesCadastroDto {

    @NotBlank(message = "Descreva o que este recibo cobre.")
    private String descricao;

    @NotNull(message = "Informe a data de emissão do recibo.")
    private LocalDate dataEmissao;

    private Long fornecedorId;

    @NotNull(message = "Informe o valor total do recibo.")
    private BigDecimal valorTotal;

    @NotNull(message = "Classifique o recibo como CUSTO ou GASTO.")
    private EnNaturezaFinanceira naturezaFinanceira;
}
