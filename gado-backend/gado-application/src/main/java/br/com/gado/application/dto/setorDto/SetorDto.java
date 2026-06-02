package br.com.gado.application.dto.setorDto;

import br.com.gado.domain.enums.EnTipoSetor;
import lombok.Data;

@Data
public class SetorDto {
    private Long id;
    private String descricao;
    private String status;
    private java.util.Date createdAt;
    private br.com.gado.application.dto.usuarioDto.UsuarioDto usuario;
    private int capacidadeMaxima;
    private String metaTexto;
    private Double metaProducaoLeite;
    private Double metaArrobaAbate;
    private EnTipoSetor setor;
}
