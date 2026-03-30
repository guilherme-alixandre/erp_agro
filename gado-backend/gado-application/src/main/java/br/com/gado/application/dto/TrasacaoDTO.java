package br.com.gado.application.dto;

import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.EParceiro;
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
