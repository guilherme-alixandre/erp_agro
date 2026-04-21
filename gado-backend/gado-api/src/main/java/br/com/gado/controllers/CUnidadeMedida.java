package br.com.gado.controllers;

import br.com.gado.application.dto.UnidadeMedidaDTO;
import br.com.gado.application.services.SUnidadeMedida;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/unidadeMedida")
public class CUnidadeMedida {

    private final SUnidadeMedida unidadeMedidaService;

    public CUnidadeMedida(SUnidadeMedida unidadeMedidaService) {
        this.unidadeMedidaService = unidadeMedidaService;
    }

    @GetMapping("/{unidadeMedidaId}")
    public UnidadeMedidaDTO getUnidadeMedida(@PathVariable Long unidadeMedidaId) {
        return unidadeMedidaService.bucarUnidadeMedidaPorId(unidadeMedidaId);
    }

    @PostMapping("/")
    public UnidadeMedidaDTO postUnidadeMedida(@RequestBody UnidadeMedidaDTO unidadeMedidaId) {
        return unidadeMedidaService.criarUnidadeMedida(unidadeMedidaId);
    }

    @DeleteMapping("/{unidadeMedidaId}")
    public String deleteUnidadeMedida(@PathVariable Long unidadeMedidaId) {
        return unidadeMedidaService.excluirUnidadeMedida(unidadeMedidaId);
    }

    @PutMapping("/{unidadeMedidaId}")
    public UnidadeMedidaDTO putUnidadeMedida(@PathVariable Long unidadeMedidaId, @RequestBody UnidadeMedidaDTO dto) {
        return unidadeMedidaService.atualizarUnidadeMedida(unidadeMedidaId, dto);
    }
}
