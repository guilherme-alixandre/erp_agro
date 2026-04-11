package br.com.gado.controllers;

import br.com.gado.application.services.SUnidadeMedida;
import br.com.gado.application.dto.UnidadeMedidaDTO;
import br.com.gado.application.dto.VacinacaoDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unidadeMedida")
public class CUnidadeMedida {

    private final SUnidadeMedida unidadeMedidaService;

    public CUnidadeMedida(SUnidadeMedida unidadeMedidaService){
        this.unidadeMedidaService = unidadeMedidaService;
    }


    @GetMapping("/{unidadeMedidaId}")
    public UnidadeMedidaDTO getUnidadeMedida(@PathVariable UnidadeMedidaDTO unidadeMedidaId){
        return unidadeMedidaService.bucarUnidadeMedidaPorId(unidadeMedidaId);
    }

    @PostMapping("/")
    public UnidadeMedidaDTO postUnidadeMedida(@RequestBody UnidadeMedidaDTO unidadeMedidaId){
        return unidadeMedidaService.criarUnidadeMedida(unidadeMedidaId);
    }

    @DeleteMapping("/{unidadeMedidaId}")
    public Boolean deleteUnidadeMedida(@PathVariable Long unidadeMedidaId){

        return unidadeMedidaService.excluirUnidadeMedida(unidadeMedidaId);
    }

    @PutMapping("/{unidadeMedidaId}")
    public UnidadeMedidaDTO putUnidadeMedida(@PathVariable UnidadeMedidaDTO unidadeMedidaId){
        return unidadeMedidaService.atualizarUnidadeMedida(unidadeMedidaId);
    }

}
