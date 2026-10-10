package br.com.gado.dto.custoAnimalDto;

import br.com.gado.enums.EnTipoCustoAnimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustoVidaEventoDto {
    private EnTipoCustoAnimal tipoCusto;
    private LocalDateTime dataEvento;
    private String descricao;
    private BigDecimal valor;
}
