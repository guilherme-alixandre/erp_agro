package br.com.gado.dto.rentabilidadeDto;

import br.com.gado.dto.lancamentoFinanceiroDto.ResumoMensalDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** DRE do período livre selecionado na aba Rentabilidade, mais o detalhamento mês a mês (para o gráfico de linha). */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumoPeriodoDto {
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private BigDecimal totalEntradas;
    private BigDecimal totalSaidasCusto;
    private BigDecimal totalSaidasDespesa;
    private BigDecimal totalSaidas;
    private BigDecimal lucroLiquido;
    private List<ResumoMensalDto> porMes;
}
