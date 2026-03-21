package br.com.gado.controllers;

import br.com.gado.application.services.SListasTarefas;
import br.com.gado.application.services.STarefa;
import br.com.gado.dto.ListasTarefasDTO;
import br.com.gado.dto.TarefaDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listaTarefa")
public class CListaTarefa {

    private final SListasTarefas listaTarefaService;

    public CListaTarefa(SListasTarefas listaTarefaService){
        this.listaTarefaService = listaTarefaService;
    }


    @GetMapping("/{listaTarefaId}")
    public ListasTarefasDTO getListaTarefa(@PathVariable Long listaTarefaId){
        return listaTarefaService.buscarListaDeTarefasPorId(listaTarefaId);
    }

    @PostMapping("/")
    public ListasTarefasDTO postListaTarefa(@RequestBody ListasTarefasDTO listaTarefa){
        return listaTarefaService.criarListaDeTarefas(listaTarefa);
    }

    @DeleteMapping("/{listaTarefaId}")
    public Boolean deleteListaTarefa(@PathVariable Long listaTarefaId){

        return listaTarefaService.excluirListaDeTarefas(listaTarefaId);
    }

    @PutMapping("/{listaTarefaId}")
    public ListasTarefasDTO putListaTarefa(@PathVariable Long listaTarefaId, @RequestBody ListasTarefasDTO listaTarefa){
        return listaTarefaService.atualizarListaDeTarefasPorId(listaTarefa, listaTarefaId);
    }

}
