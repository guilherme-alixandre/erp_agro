package br.com.gado.dto;

import br.com.gado.domain.entities.EListasTarefas;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TarefaDTO {
    private Long id;
    private String descricao;
    private LocalDateTime dataLimite;
    private boolean status;
    private EListasTarefas lista;
}
