package br.com.gado.controllers;

import br.com.gado.application.services.SInsumo;
import br.com.gado.dto.InsumoDto;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/insumos")
public class CInsumo {

    private final SInsumo insumoService;

    public CInsumo(SInsumo insumoService){
        this.insumoService = insumoService;
    }


    @GetMapping("/{id}")
    public Map<String, Object> getInsumo(@PathVariable Long id){
        return insumoService.buscaPorId(id);
    }

    @PostMapping("/")
    public String postInsumo(@RequestBody InsumoDto dto){
        return insumoService.cadastraInsumo(dto);
    }

    @DeleteMapping("/{id}")
    public String deleteInsumo(@PathVariable Long id){
        return insumoService.deletaInsumo(id);
    }

    @PutMapping("/{id}")
    public String putInsumo(@PathVariable Long id,
                            @RequestBody InsumoDto dto)
    {
        return insumoService.alteraInsumo(id, dto);
    }

}
