package br.com.gado.dto.insumoDto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * Edição de dados sensíveis do insumo (nome, unidades, fator de conversão, preços,
 * estoque mínimo, fornecedor). Restrita a ADMINISTRADOR, GERENTE e CUIDADOR_CHEFE.
 * O saldoAtual NÃO é editável aqui — só muda via entrada de estoque ou consumo,
 * para preservar a integridade do saldo.
 */
@Data
public class InsumoEstoquePutDto {

    private String nome;

    private Long unidadeMedidaSecundariaId;

    @Positive(message = "O fator de conversão deve ser maior que zero.")
    private Double fatorConversao;

    @PositiveOrZero(message = "O estoque mínimo não pode ser negativo.")
    private Double estoqueMinimo;

    @PositiveOrZero(message = "O preço de compra médio não pode ser negativo.")
    private Double precoCompraMedio;

    @PositiveOrZero(message = "O preço da última compra não pode ser negativo.")
    private Double precoUltimaCompra;

    private Long parceiroId;
}
