package br.com.gado.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ETransacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime data;
    private Double valor;

    @ManyToOne
    @JoinColumn(name = "parceiro_id")
    private EParceiro parceiro;

    // necessário, depois explico, ligado com EntityLote
    @ManyToOne
    @JoinColumn(name = "lote_id")
    private ELote lote;

}
