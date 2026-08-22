package br.com.gado.dto.insumoDto;

import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoInsumo;
import lombok.Data;

@Data
public class InsumoEstoqueRespostaDto {
    private Long id;
    private String nome;
    private EnTipoInsumo tipo;
    private EnStatus status;

    private Long grupoProdutoId;
    private String grupoProdutoNome;
    private String codigoProduto;

    private Double saldoAtual;
    private Double estoqueMinimo;
    private Boolean abaixoDoEstoqueMinimo;

    private Long unidadeMedidaPrimariaId;
    private String unidadeMedidaPrimariaSigla;

    private Long unidadeMedidaSecundariaId;
    private String unidadeMedidaSecundariaSigla;

    private Double fatorConversao;

    private Double precoCompraMedio;
    private Double precoUltimaCompra;

    private String numeroNf;
    private String chaveAcessoNf;

    private Long parceiroId;
    private String parceiroNome;
}
