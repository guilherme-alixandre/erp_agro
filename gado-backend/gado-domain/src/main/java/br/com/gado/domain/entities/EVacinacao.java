package br.com.gado.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "vacinacao")
@Data
public class EVacinacao extends EAbstract{

    private Date dataOcorrencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EAnimal animalRelacionado;

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
