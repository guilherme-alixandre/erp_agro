package br.com.gado.dto.resumoDto;

import br.com.gado.dto.tarefaDto.TarefaRespostaDto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ResumoDto {

    /** true para ADMINISTRADOR/GERENTE — só nesse caso os campos abaixo (até tarefasPendentes) vêm preenchidos. */
    private boolean financeiroVisivel;

    private Long totalAnimais;
    private Long totalLotes;
    private Long totalSetores;

    private BigDecimal vendasMensais;
    private BigDecimal gastosMensais;
    private BigDecimal lucroLiquido;

    private String maiorFonteReceitaLabel;
    private BigDecimal maiorFonteReceitaValor;
    private String menorFonteReceitaLabel;
    private BigDecimal menorFonteReceitaValor;

    /** Abertos a qualquer perfil. */
    private List<AlertaEstoqueResumoDto> alertasEstoque = new ArrayList<>();
    private List<TarefaRespostaDto> tarefasPendentes = new ArrayList<>();
}
