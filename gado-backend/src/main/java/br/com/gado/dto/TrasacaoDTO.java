package br.com.gado.dto;

import br.com.gado.entities.ELote;
import br.com.gado.entities.EParceiro;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class TrasacaoDTO extends AbstractDTO {

    private Date data;
    private Double valor;
    private EParceiro parceiro;
    private ELote lote;
}
