package br.com.gado.dto.documentoEntradaDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Payload da ação "Vincular" — de propósito só tem o id do produto: liga o item ao catálogo
 * sem expor nenhum campo de valor, reforçando estruturalmente que o perfil Financeiro não
 * pode alterar valores por essa via (ver SDocumentoEntrada.vincularProduto). */
@Data
public class VincularProdutoDto {

    @NotNull(message = "Informe o produto do catálogo a vincular.")
    private Long produtoId;
}
