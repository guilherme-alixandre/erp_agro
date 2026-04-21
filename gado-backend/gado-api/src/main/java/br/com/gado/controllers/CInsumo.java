package br.com.gado.controllers;

import br.com.gado.application.dto.InsumoDto;
import br.com.gado.application.services.SInsumo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insumos")
public class CInsumo {

    @Autowired
    private SInsumo insumoService;

    @GetMapping("/{id}")
    public InsumoDto getInsumo(@PathVariable Long id) {
        return insumoService.buscaPorId(id);
    }

    @PostMapping("/")
    public InsumoDto postInsumo(@RequestBody InsumoDto dto) {
        return insumoService.cadastraInsumo(dto);
    }

    @DeleteMapping("/{id}")
    public String deleteInsumo(@PathVariable Long id) {
        return insumoService.deletaInsumo(id);
    }

    @PutMapping("/{id}")
    public InsumoDto putInsumo(@PathVariable Long id, @RequestBody InsumoDto dto) {
        return insumoService.alteraInsumo(id, dto);
    }
}
