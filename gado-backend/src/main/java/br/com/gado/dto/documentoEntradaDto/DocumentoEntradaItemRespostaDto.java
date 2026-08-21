package br.com.gado.dto.documentoEntradaDto;

import br.com.gado.enums.EnNaturezaFinanceira;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DocumentoEntradaItemRespostaDto {
    private Long id;
    private String descricaoXml;
    private String codigoXml;
    private Long produtoId;
    private String produtoNome;
    private BigDecimal quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
    private Boolean vinculado;
    private String vinculadoPorEmail;
    private LocalDateTime vinculadoEm;
    private EnNaturezaFinanceira naturezaFinanceira;
}
