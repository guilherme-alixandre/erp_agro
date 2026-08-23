package br.com.gado.controllers;

import br.com.gado.dto.tarefaDto.TarefaAtribuirDto;
import br.com.gado.dto.tarefaDto.TarefaEdicaoDto;
import br.com.gado.dto.tarefaDto.TarefaRespostaDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.STarefa;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Cada usuário tem uma lista de tarefas própria; qualquer usuário pode atribuir uma tarefa a qualquer outro. */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/tarefas")
public class CTarefa {

    private final STarefa tarefaService;

    public CTarefa(STarefa tarefaService) {
        this.tarefaService = tarefaService;
    }

    @GetMapping
    public List<TarefaRespostaDto> listarMinhasTarefas() {
        return tarefaService.listarMinhasTarefas(SecurityUtils.currentUserEmail());
    }

    @PostMapping
    public ResponseEntity<TarefaRespostaDto> atribuirTarefa(@Valid @RequestBody TarefaAtribuirDto dto) {
        TarefaRespostaDto criada = tarefaService.atribuirTarefa(dto, SecurityUtils.currentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/{id}")
    public TarefaRespostaDto editarTarefa(@PathVariable Long id, @RequestBody TarefaEdicaoDto dto) {
        return tarefaService.editarTarefa(id, dto, SecurityUtils.currentUserEmail());
    }

    @DeleteMapping("/{id}")
    public String excluirTarefa(@PathVariable Long id) {
        return tarefaService.excluirTarefa(id, SecurityUtils.currentUserEmail());
    }
}
