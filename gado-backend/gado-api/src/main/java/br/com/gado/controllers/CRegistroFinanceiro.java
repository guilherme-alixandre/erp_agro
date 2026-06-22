package br.com.gado.controllers;

import br.com.gado.application.dto.RegistroFinanceiroDTO;
import br.com.gado.application.services.SRegistroFinanceiro;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registroFinanceiro")
public class CRegistroFinanceiro {

    private final SRegistroFinanceiro registroFinanceiroService;

    public CRegistroFinanceiro(SRegistroFinanceiro registroFinanceiroService) {
        this.registroFinanceiroService = registroFinanceiroService;
    }

    @GetMapping("/{registroFinanceiroId}")
    public RegistroFinanceiroDTO getMovimentacaoEsotque(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long registroFinanceiroId) {
        return registroFinanceiroService.buscarRegistroFinanceiroPorId(emailUsuario, registroFinanceiroId);
    }

    @PostMapping("/")
    public RegistroFinanceiroDTO postMovimentacaoEstoque(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @RequestBody RegistroFinanceiroDTO registroFinanceiroDto) {
        return registroFinanceiroService.criarRegistroFinanceiro(emailUsuario, registroFinanceiroDto);
    }

    @DeleteMapping("/{registroFinanceiroId}")
    public String deleteMovimentacaoEsotque(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long registroFinanceiroId) {
        return registroFinanceiroService.excluirRegistroFinanceiroPorId(emailUsuario, registroFinanceiroId);
    }

    @PutMapping("/{registroFinanceiroId}")
    public RegistroFinanceiroDTO putMovimentacaoEstoque(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long registroFinanceiroId,
            @RequestBody RegistroFinanceiroDTO dto) {
        return registroFinanceiroService.atualizarRegistroFinanceiroPorId(emailUsuario, registroFinanceiroId, dto);
    }
}
