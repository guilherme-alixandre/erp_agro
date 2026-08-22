package br.com.gado.dto.documentoSaidaDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Venda de leite. Informe precoLitro OU valorTotal (o outro é calculado a partir da soma dos
 * litros de todos os lotes informados).
 */
@Data
public class VendaLeiteCadastroDto {

    @NotNull(message = "Informe a data da venda.")
    private LocalDate dataEmissao;

    private String numeroDocumento;
    private String chaveAcesso;

    private BigDecimal precoLitro;
    private BigDecimal valorTotal;

    @NotEmpty(message = "Selecione ao menos um lote de origem do leite.")
    @Valid
    private List<VendaLeiteItemCadastroDto> itens;
}
