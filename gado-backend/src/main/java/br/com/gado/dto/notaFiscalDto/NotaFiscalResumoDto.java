package br.com.gado.dto.notaFiscalDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Linha unificada da listagem de "Notas Fiscais": une EDocumentoEntrada (direcao ENTRADA) e
 * EDocumentoSaida (direcao RECEITA) num único formato para a tela de NFs, que filtra por
 * número, chave de acesso e direção (Entrada/Receita).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotaFiscalResumoDto {
    private Long id;
    private String direcao; // ENTRADA | RECEITA
    private String tipoDocumento; // NF_E | RECIBO_SIMPLES | VENDA_LEITE | VENDA_ANIMAL
    private String numeroDocumento;
    private String chaveAcesso;
    private LocalDate dataEmissao;
    private BigDecimal valorTotal;
    private String status; // status de aprovação (entradas) ou "-" (saídas, sempre confirmadas)
}
