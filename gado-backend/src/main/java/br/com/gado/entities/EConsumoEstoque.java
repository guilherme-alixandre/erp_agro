package br.com.gado.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "consumo_estoque")
@Data
public class EConsumoEstoque extends EAbstract {

    /** Motivo/finalidade da saída de estoque, informado pelo usuário. */
    @Column(nullable = false)
    private String motivo;

    @Column(name = "data_consumo", nullable = false)
    private LocalDateTime dataConsumo;

    /** E-mail de quem registrou (qualquer perfil). Guardado como texto, como em EConsumoInsumo. */
    @Column(name = "criado_por_email", nullable = false)
    private String criadoPorEmail;

    @Column(nullable = false)
    private Boolean cancelado = false;

    /** Justificativa obrigatória do cancelamento — preenchida apenas quando cancelado = true. */
    @Column(name = "motivo_cancelamento")
    private String motivoCancelamento;

    @Column(name = "cancelado_por_email")
    private String canceladoPorEmail;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @OneToMany(mappedBy = "consumoEstoque", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EConsumoEstoqueItem> itens = new ArrayList<>();
}
