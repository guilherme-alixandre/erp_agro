package br.com.gado.application.dto.loteDto;

import br.com.gado.application.dto.usuarioDto.UsuarioDto;
import br.com.gado.domain.enums.EnStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoteDto {
    private Long id;
    private String descricao;
    private String racaPredominante;
    private EnStatus status;
    private LocalDateTime created_at;
    private UsuarioDto usuario;
}
