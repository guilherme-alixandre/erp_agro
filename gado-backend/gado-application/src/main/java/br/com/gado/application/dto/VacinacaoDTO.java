package br.com.gado.application.dto;

import br.com.gado.domain.entities.EInsumo;
import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.EUsuario;
import lombok.Data;

import java.util.Date;

@Data
public class VacinacaoDTO extends AbstractDTO{

    private Date dataOcorrencia;
    private EUsuario usuarioRelacionado;
    private ELote loteRelacionado;
    private EInsumo insumoRelacionado;
}
