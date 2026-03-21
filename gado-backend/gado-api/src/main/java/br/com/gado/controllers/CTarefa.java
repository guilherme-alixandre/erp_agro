package br.com.gado.controllers;

import br.com.gado.application.services.STarefa;
import br.com.gado.dto.TarefaDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tarefa")
public class CTarefa {

    private final STarefa tarefaService;

    public CTarefa(STarefa tarefaService){
        this.tarefaService = tarefaService;
    }


    @GetMapping("/{tarefaId}")
    public TarefaDTO getTarefa(@PathVariable Long tarefaId){
        return tarefaService.buscarTarefaPorId(tarefaId);
    }

    @PostMapping("/")
    public TarefaDTO postTarefa(@RequestBody TarefaDTO tarefa, Long listaId){
        return tarefaService.criarTarefa(tarefa, listaId);
    }

    @DeleteMapping("/{tarefaId}")
    public Boolean deleteTarefa(@PathVariable Long tarefaId){

        return tarefaService.excluirTarefa(tarefaId);
    }

    @PutMapping("/{tarefaId}")
    public TarefaDTO putTarefa(@PathVariable Long tarefaId, @RequestBody TarefaDTO tarefa){
        return tarefaService.atualizarTarefa(tarefa, tarefaId);
    }

}
