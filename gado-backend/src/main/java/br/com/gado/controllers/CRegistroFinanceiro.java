package br.com.gado.controllers;

import br.com.gado.dto.RegistroFinanceiroDTO;
import br.com.gado.services.SRegistroFinanceiro;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/registroFinanceiro")
public class CRegistroFinanceiro {

    private final SRegistroFinanceiro registroFinanceiroService;

    public CRegistroFinanceiro(SRegistroFinanceiro registroFinanceiroService) {
        this.registroFinanceiroService = registroFinanceiroService;
    }

    @GetMapping("/{registroFinanceiroId}")
    public RegistroFinanceiroDTO getMovimentacaoEsotque(@PathVariable Long registroFinanceiroId) {
        return registroFinanceiroService.buscarRegistroFinanceiroPorId(registroFinanceiroId);
    }

    @PostMapping("/")
    public RegistroFinanceiroDTO postMovimentacaoEstoque(@RequestBody RegistroFinanceiroDTO registroFinanceiroId) {
        return registroFinanceiroService.criarRegistroFinanceiro(registroFinanceiroId);
    }

    @DeleteMapping("/{registroFinanceiroId}")
    public String deleteMovimentacaoEsotque(@PathVariable Long registroFinanceiroId) {
        return registroFinanceiroService.excluirRegistroFinanceiroPorId(registroFinanceiroId);
    }

    @PutMapping("/{registroFinanceiroId}")
    public RegistroFinanceiroDTO putMovimentacaoEstoque(@PathVariable Long registroFinanceiroId, @RequestBody RegistroFinanceiroDTO dto) {
        return registroFinanceiroService.atualizarRegistroFinanceiroPorId(registroFinanceiroId, dto);
    }
}
