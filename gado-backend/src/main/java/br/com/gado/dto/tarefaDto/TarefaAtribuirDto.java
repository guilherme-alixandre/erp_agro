package br.com.gado.dto.tarefaDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class TarefaAtribuirDto {

    @NotBlank(message = "Informe a descrição da tarefa.")
    private String descricao;

    private Date dataLimite;

    @NotBlank(message = "Informe para quem a tarefa deve ser atribuída.")
    private String atribuidoParaEmail;
}
