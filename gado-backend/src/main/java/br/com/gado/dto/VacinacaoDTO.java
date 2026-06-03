package br.com.gado.dto;

import br.com.gado.entities.EInsumo;
import br.com.gado.entities.ELote;
import br.com.gado.entities.EUsuario;
import lombok.Data;

import java.util.Date;

@Data
public class VacinacaoDTO extends AbstractDTO{

    private Date dataOcorrencia;
    private EUsuario usuarioRelacionado;
    private ELote loteRelacionado;
    private EInsumo insumoRelacionado;
}
