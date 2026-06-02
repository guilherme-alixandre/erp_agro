package br.com.gado.domain.entities;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "vacinacao")
@Data
public class EVacinacao extends EAbstract{

    private Date dataOcorrencia;

    @ManyToOne
    @JoinColumn(name = "usuario_id_id")
    private EUsuario usuarioRelacionado;
    @ManyToOne
    @JoinColumn(name = "lote_id_id")
    private ELote loteRelacionado;
    @ManyToOne
    @JoinColumn(name = "insumo_id_id")
    private EInsumo insumoRelacionado;
}
