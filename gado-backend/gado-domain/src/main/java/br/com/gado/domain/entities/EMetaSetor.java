package br.com.gado.domain.entities;

import br.com.gado.domain.enums.EnTipoGado;
import br.com.gado.domain.enums.EnTipoMeta;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "meta_setor")
@Data
public class EMetaSetor extends EAbstract {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", nullable = false)
    private ESetor setor;

    @Column(name = "data_inicial", nullable = false)
    private LocalDate dataInicial;

    @Column(name = "data_final", nullable = false)
    private LocalDate dataFinal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_meta", nullable = false)
    private EnTipoMeta tipoMeta;

    @Column(name = "quantidade_esperada", nullable = false)
    private Double quantidadeEsperada;

    /**
     * Preço médio: R$/Litro para LEITE, R$/Arroba para ARROBA.
     */
    @Column(name = "preco_medio", nullable = false)
    private Double precoMedio;

    /**
     * Tipo de gado para cálculo de rendimento de carcaça.
     * Obrigatório quando tipoMeta == ARROBA. Deve ser nulo quando tipoMeta == LEITE.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_gado")
    private EnTipoGado tipoGado;

    @OneToMany(mappedBy = "metaSetor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EMedicaoMeta> medicoes = new ArrayList<>();
}
