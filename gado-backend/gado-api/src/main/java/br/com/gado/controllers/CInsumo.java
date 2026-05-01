package br.com.gado.controllers;

import br.com.gado.application.services.SInsumo;
import br.com.gado.application.dto.InsumoDto;
import br.com.gado.application.dto.InsumoRespostaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/insumos")
public class CInsumo {

    @Autowired
    private SInsumo insumoService;


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

    @GetMapping("/vacinas")
    public List<InsumoRespostaDto> listarVacinas(
            @RequestParam(name = "busca", required = false, defaultValue = "") String busca){
        return insumoService.listarVacinas(busca);
    }

    @PostMapping("/vacinas")
    public InsumoRespostaDto postVacina(@RequestBody InsumoDto dto){
        return insumoService.cadastraVacina(dto);
    }

    @PutMapping("/vacinas/{id}")
    public InsumoRespostaDto putVacina(@PathVariable Long id,
                                       @RequestBody InsumoDto dto){
        return insumoService.alteraVacina(id, dto);
    }

    @DeleteMapping("/vacinas/{id}")
    public String deleteVacina(@PathVariable Long id){
        return insumoService.deletaVacina(id);
    }

}
