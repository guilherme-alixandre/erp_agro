package br.com.gado.dto.tarefaDto;

import lombok.Data;

import java.util.Date;

@Data
public class TarefaRespostaDto {

    private Long id;
    private String descricao;
    private Date dataLimite;
    private boolean statusConclusao;
    private String atribuidoPorEmail;
    private String atribuidoPorNome;
    private String atribuidoParaEmail;
    private String atribuidoParaNome;
    private Date createdAt;
}
