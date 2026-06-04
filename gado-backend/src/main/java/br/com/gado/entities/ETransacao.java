package br.com.gado.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transacao")
@Data
public class ETransacao extends EAbstract{

    private Date data;
    private Double valor;

    @ManyToOne
    @JoinColumn(name = "parceiro_id")
    private EParceiro parceiro;

    // necessário, depois explico, ligado com ELote
    @ManyToOne
    @JoinColumn(name = "lote_id")
    private ELote lote;

}
