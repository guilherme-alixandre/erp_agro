package br.com.gado.dto.documentoEntradaDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Correção pontual de dados extraídos de uma NF-e (ex: erro de leitura do XML).
 * Os campos de dados são opcionais — só o que vier preenchido é alterado — mas
 * senhaConfirmacao é sempre obrigatória: é a dupla validação de segurança exigida de
 * Admin/Gerente para qualquer edição — ver SDocumentoEntrada.editarNfe.
 */
@Data
public class NfeUpdateDto {
    private String numeroDocumento;
    private String serie;
    private LocalDate dataEmissao;
    private Long fornecedorId;
    private BigDecimal valorTotal;
    private List<NfeItemUpdateDto> itens;

    @NotBlank(message = "A senha de confirmação é obrigatória para editar uma NF-e.")
    private String senhaConfirmacao;
}
