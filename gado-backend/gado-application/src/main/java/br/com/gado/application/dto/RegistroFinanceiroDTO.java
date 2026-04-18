package br.com.gado.application.dto;

import br.com.gado.domain.entities.ECategoria;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnStatusDespesa;
import br.com.gado.domain.enums.EnTipoDespesa;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class RegistroFinanceiroDTO extends AbstractDTO {

    private String descricao;
    private EnTipoDespesa tipoDespesa;
    private Double valor;
    private Date dataVencimento;
    private Date dataPagamento;
    private EnStatusDespesa statusDespesa;

    private ECategoria categoriaId;
    private EUsuario usuarioId;
}
