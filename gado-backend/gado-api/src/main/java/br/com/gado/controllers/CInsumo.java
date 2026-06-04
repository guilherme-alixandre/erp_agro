package br.com.gado.controllers;

import br.com.gado.application.dto.InsumoDto;
import br.com.gado.application.dto.insumoDto.VacinaCadastroDto;
import br.com.gado.application.dto.insumoDto.VacinaPutDto;
import br.com.gado.application.services.SInsumo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insumos")
public class CInsumo {

    @Autowired
    private SInsumo insumoService;

    @GetMapping("/vacinas")
    public List<InsumoDto> getVacinas(@RequestParam(required = false) String busca) {
        return insumoService.listarVacinas(busca);
    }

    @PostMapping("/vacinas")
    public InsumoDto postVacina(@RequestBody VacinaCadastroDto dto) {
        return insumoService.criarVacina(dto);
    }

    @PutMapping("/vacinas/{id}")
    public InsumoDto putVacina(@PathVariable Long id, @RequestBody VacinaPutDto dto) {
        return insumoService.atualizarVacina(id, dto);
    }

    @DeleteMapping("/vacinas/{id}")
    public String deleteVacina(@PathVariable Long id) {
        return insumoService.deletarVacina(id);
    }

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
