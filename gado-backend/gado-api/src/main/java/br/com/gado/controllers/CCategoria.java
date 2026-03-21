package br.com.gado.controllers;

import br.com.gado.application.services.SCategoria;
import br.com.gado.dto.CategoriaDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categoria")
public class CCategoria {

    private final SCategoria categoriaService;

    public CCategoria(SCategoria categoriaService) {
        this.categoriaService = categoriaService;
    }


    @GetMapping("/{categoriaId}")
    public CategoriaDTO getCategoria(@PathVariable Long categoriaId){
        return categoriaService.buscarCategoriaPorId(categoriaId);
    }

    @PostMapping("/")
    public CategoriaDTO postCategoria(@RequestBody CategoriaDTO categoria){
        return categoriaService.criarCategoria(categoria);
    }

    @DeleteMapping("/{categoriaId}")
    public Boolean deleteCategoria(@PathVariable Long categoriaId){

        return categoriaService.excluirCategoria(categoriaId);
    }

    @PutMapping("/{categoriaId}")
    public CategoriaDTO putCategoria(@PathVariable Long categoriaId, @RequestBody CategoriaDTO categoria){
        return categoriaService.atualizarCategoriaPorId(categoria, categoriaId);
    }

}
