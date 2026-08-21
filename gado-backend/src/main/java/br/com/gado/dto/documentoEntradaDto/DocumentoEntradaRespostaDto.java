package br.com.gado.dto.documentoEntradaDto;

import br.com.gado.enums.EnStatusAprovacaoFinanceira;
import br.com.gado.enums.EnTipoDocumentoFinanceiro;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocumentoEntradaRespostaDto {
    private Long id;
    private EnTipoDocumentoFinanceiro tipoDocumento;
    private EnStatusAprovacaoFinanceira statusAprovacao;
    private String numeroDocumento;
    private String serie;
    private String chaveAcessoNfe;
    private LocalDate dataEmissao;
    private LocalDate dataEntrada;
    private Long fornecedorId;
    private String fornecedorNome;
    private BigDecimal valorTotal;
    private String justificativaRecusa;
    private String criadoPorEmail;
    private String aprovadoPorEmail;
    private LocalDateTime aprovadoEm;
    private String ultimaEdicaoPorEmail;
    private LocalDateTime ultimaEdicaoEm;
    private List<DocumentoEntradaItemRespostaDto> itens;
}
