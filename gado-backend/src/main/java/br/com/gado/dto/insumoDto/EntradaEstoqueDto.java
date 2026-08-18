package br.com.gado.dto.insumoDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entrada manual de estoque. Restrita a ADMINISTRADOR, GERENTE e CUIDADOR_CHEFE.
 * A arquitetura já isola esta operação (Service.registrarEntradaEstoque) para que,
 * no futuro, um serviço de importação de XML de NF possa chamar o mesmo método
 * a partir dos dados extraídos da nota, sem duplicar a regra de recálculo de preço médio.
 */
@Data
public class EntradaEstoqueDto {

    @NotNull(message = "A quantidade da entrada é obrigatória.")
    @Positive(message = "A quantidade da entrada deve ser maior que zero.")
    private Double quantidade; // sempre na unidade primária do insumo

    @NotNull(message = "O preço unitário de compra é obrigatório.")
    @Positive(message = "O preço unitário de compra deve ser maior que zero.")
    private Double precoUnitario; // preço por unidade primária, nesta compra

    private Long parceiroId;

    private String numeroNf;

    private String chaveAcessoNf;

    /** Se não informada, o Service usa LocalDateTime.now(). */
    private LocalDateTime dataEntrada;
}
