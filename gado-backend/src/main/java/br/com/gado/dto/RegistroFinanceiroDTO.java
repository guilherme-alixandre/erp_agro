package br.com.gado.dto;

import br.com.gado.entities.ECategoria;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnStatusDespesa;
import br.com.gado.enums.EnTipoDespesa;
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
