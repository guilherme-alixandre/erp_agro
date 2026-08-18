package br.com.gado.dto.insumoDto;

import br.com.gado.enums.EnTipoInsumo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class InsumoEstoqueCadastroDto {

    @NotBlank(message = "O nome do insumo é obrigatório.")
    private String nome;

    @NotNull(message = "O tipo do insumo é obrigatório.")
    private EnTipoInsumo tipo;

    @NotNull(message = "O grupo de produto é obrigatório.")
    private Long grupoProdutoId;

    @NotNull(message = "A unidade de medida primária é obrigatória.")
    private Long unidadeMedidaPrimariaId;

    /** Opcional: unidade usada no dia a dia do consumo (ex: KG quando a primária é SACA). */
    private Long unidadeMedidaSecundariaId;

    /** Obrigatório quando unidadeMedidaSecundariaId for informado. */
    @Positive(message = "O fator de conversão deve ser maior que zero.")
    private Double fatorConversao;

    @PositiveOrZero(message = "O estoque mínimo não pode ser negativo.")
    private Double estoqueMinimo;

    private Long parceiroId;
}
