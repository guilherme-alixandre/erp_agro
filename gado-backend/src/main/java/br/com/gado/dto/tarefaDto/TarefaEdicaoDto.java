package br.com.gado.dto.tarefaDto;

import lombok.Data;

import java.util.Date;

@Data
public class TarefaEdicaoDto {

    private String descricao;
    private Date dataLimite;
    private Boolean statusConclusao;
}
