package br.com.gado.dto.custoAnimalDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Custo de vida acumulado de um animal — soma histórica de aquisição, alimentação e vacinação
 * (ver ECustoAnimal). Diferente do custo de ração por lote (CustoRacaoLoteDto), aqui o preço é
 * fotografado no momento de cada evento e o registro pertence ao animal: ao trocar de lote, o
 * custo acumulado continua sendo dele. Eventos registrados antes desta feature não aparecem aqui
 * — não há como reconstruir retroativamente o preço pago em consumos/vacinações antigos.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustoVidaAnimalDto {
    private Long animalId;
    private BigDecimal totalAquisicao;
    private BigDecimal totalAlimentacao;
    private BigDecimal totalVacinacao;
    private BigDecimal totalGeral;
    private List<CustoVidaEventoDto> eventos;
}
