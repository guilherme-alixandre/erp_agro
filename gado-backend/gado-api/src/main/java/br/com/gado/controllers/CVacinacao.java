package br.com.gado.controllers;

import br.com.gado.application.services.STarefa;
import br.com.gado.application.services.SVacinacao;
import br.com.gado.dto.TarefaDTO;
import br.com.gado.dto.VacinacaoDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vacinacao")
public class CVacinacao {

    private final SVacinacao vacinacaoService;

    public CVacinacao(SVacinacao vacinacaoService){
        this.vacinacaoService = vacinacaoService;
    }


    @GetMapping("/{vacinacaoId}")
    public VacinacaoDTO getVacinacao(@PathVariable VacinacaoDTO vacinacaoId){
        return vacinacaoService.buscarVacinacaoPorId(vacinacaoId);
    }

    @PostMapping("/")
    public VacinacaoDTO postVacinacao(@RequestBody VacinacaoDTO vacinacao){
        return vacinacaoService.criarVacinacao(vacinacao);
    }

    @DeleteMapping("/{vacinacaoId}")
    public Boolean deleteVacinacao(@PathVariable Long vacinacaoId){

        return vacinacaoService.excluirVacinacaoPorId(vacinacaoId);
    }

    @PutMapping("/{vacinacaoId}")
    public VacinacaoDTO putVacinacao(@PathVariable VacinacaoDTO vacinacaoId){
        return vacinacaoService.atualizarVacinacaoPorId(vacinacaoId);
    }

}
