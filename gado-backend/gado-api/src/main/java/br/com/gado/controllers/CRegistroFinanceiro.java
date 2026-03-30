package br.com.gado.controllers;

import br.com.gado.application.services.SRegistroFinanceiro;
import br.com.gado.application.dto.RegistroFinanceiroDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registroFinanceiro")
public class CRegistroFinanceiro {

    private final SRegistroFinanceiro registroFinanceiroService;

    public CRegistroFinanceiro(SRegistroFinanceiro registroFinanceiroService){
        this.registroFinanceiroService = registroFinanceiroService;
    }


    @GetMapping("/{registroFinanceiroId}")
    public RegistroFinanceiroDTO getMovimentacaoEsotque(@PathVariable RegistroFinanceiroDTO registroFinanceiroId){
        return registroFinanceiroService.buscarRegistroFinanceiroPorId(registroFinanceiroId);
    }

    @PostMapping("/")
    public RegistroFinanceiroDTO postMovimentacaoEstoque(@RequestBody RegistroFinanceiroDTO registroFinanceiroId){
        return registroFinanceiroService.criarRegistroFinanceiro(registroFinanceiroId);
    }

    @DeleteMapping("/{registroFinanceiroId}")
    public Boolean deleteMovimentacaoEsotque(@PathVariable Long registroFinanceiroId){

        return registroFinanceiroService.excluirRegistroFinanceiroPorId(registroFinanceiroId);
    }

    @PutMapping("/{registroFinanceiroId}")
    public RegistroFinanceiroDTO putMovimentacaoEstoque(@PathVariable RegistroFinanceiroDTO registroFinanceiroId){
        return registroFinanceiroService.atualizarRegistroFinanceiroPorId(registroFinanceiroId);
    }

}
