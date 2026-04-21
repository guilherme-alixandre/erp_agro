package br.com.gado.controllers;

import br.com.gado.application.dto.TarefaDTO;
import br.com.gado.application.services.STarefa;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tarefa")
public class CTarefa {

    private final STarefa tarefaService;

    public CTarefa(STarefa tarefaService) {
        this.tarefaService = tarefaService;
    }

    @GetMapping("/{tarefaId}")
    public TarefaDTO getTarefa(@PathVariable Long tarefaId) {
        return tarefaService.buscarTarefaPorId(tarefaId);
    }

    @PostMapping("/")
    public TarefaDTO postTarefa(@RequestBody TarefaDTO tarefa, @RequestParam Long listaId) {
        return tarefaService.criarTarefa(tarefa, listaId);
    }

    @DeleteMapping("/{tarefaId}")
    public String deleteTarefa(@PathVariable Long tarefaId) {
        return tarefaService.excluirTarefa(tarefaId);
    }

    @PutMapping("/{tarefaId}")
    public TarefaDTO putTarefa(@PathVariable Long tarefaId, @RequestBody TarefaDTO tarefa) {
        return tarefaService.atualizarTarefa(tarefa, tarefaId);
    }
}
