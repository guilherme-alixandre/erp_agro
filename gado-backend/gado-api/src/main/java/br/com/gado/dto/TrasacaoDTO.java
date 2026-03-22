package br.com.gado.dto;

import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.EParceiro;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class TrasacaoDTO extends AbstractDTO {

    private Date data;
    private Double valor;
    private EParceiro parceiro;
    private ELote lote;
}
