package br.com.gado.dto.movimentacaoEstoqueDto;

import br.com.gado.enums.EnTipoMovimentacaoEstoque;
import lombok.Data;

import java.util.Date;

@Data
public class MovimentacaoEstoqueRespostaDto {

    private Long id;
    private EnTipoMovimentacaoEstoque tipo;
    private double quantidade;
    private double valorUnitario;
    private Date dataMovimentacao;
    private Long insumoId;
    private String insumoNome;
    private String unidadeMedidaSigla;
    private Long parceiroId;
    private String parceiroNome;
    private Long setorId;
    private String setorNome;
    private Long animalId;
    private String animalCodigoBrinco;
}
