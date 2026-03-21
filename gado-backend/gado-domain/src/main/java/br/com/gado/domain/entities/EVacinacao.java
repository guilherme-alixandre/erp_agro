package br.com.gado.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.util.Date;

@Entity
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
