package br.com.gado.controllers;

import br.com.gado.dto.CategoriaDTO;
import br.com.gado.services.SCategoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/categoria")
public class CCategoria {

    @Autowired
    private SCategoria categoriaService;

    @GetMapping("/{categoriaId}")
    public CategoriaDTO getCategoria(@PathVariable Long categoriaId) {
        return categoriaService.buscarCategoriaPorId(categoriaId);
    }

    @PostMapping("/")
    public CategoriaDTO postCategoria(@RequestBody CategoriaDTO categoria) {
        return categoriaService.criarCategoria(categoria);
    }

    @DeleteMapping("/{categoriaId}")
    public String deleteCategoria(@PathVariable Long categoriaId) {
        return categoriaService.excluirCategoria(categoriaId);
    }

    @PutMapping("/{categoriaId}")
    public CategoriaDTO putCategoria(@PathVariable Long categoriaId, @RequestBody CategoriaDTO categoria) {
        return categoriaService.atualizarCategoriaPorId(categoria, categoriaId);
    }
}
