package br.com.gado.controllers;

import br.com.gado.dto.ListasTarefasDTO;
import br.com.gado.services.SListasTarefas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/listaTarefa")
public class CListaTarefa {

    @Autowired
    private SListasTarefas listaTarefaService;

    @GetMapping("/{listaTarefaId}")
    public ListasTarefasDTO getListaTarefa(@PathVariable Long listaTarefaId) {
        return listaTarefaService.buscarListaDeTarefasPorId(listaTarefaId);
    }

    @PostMapping("/")
    public ListasTarefasDTO postListaTarefa(@RequestBody ListasTarefasDTO listaTarefa) {
        return listaTarefaService.criarListaDeTarefas(listaTarefa);
    }

    @DeleteMapping("/{listaTarefaId}")
    public String deleteListaTarefa(@PathVariable Long listaTarefaId) {
        return listaTarefaService.excluirListaDeTarefas(listaTarefaId);
    }

    @PutMapping("/{listaTarefaId}")
    public ListasTarefasDTO putListaTarefa(@PathVariable Long listaTarefaId, @RequestBody ListasTarefasDTO listaTarefa) {
        return listaTarefaService.atualizarListaDeTarefasPorId(listaTarefa, listaTarefaId);
    }
}
