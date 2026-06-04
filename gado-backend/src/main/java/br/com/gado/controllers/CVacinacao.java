package br.com.gado.controllers;

import br.com.gado.dto.VacinacaoDTO;
import br.com.gado.services.SVacinacao;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/vacinacao")
public class CVacinacao {

    private final SVacinacao vacinacaoService;

    public CVacinacao(SVacinacao vacinacaoService) {
        this.vacinacaoService = vacinacaoService;
    }

    @GetMapping("/{vacinacaoId}")
    public VacinacaoDTO getVacinacao(@PathVariable Long vacinacaoId) {
        return vacinacaoService.buscarVacinacaoPorId(vacinacaoId);
    }

    @PostMapping("/")
    public VacinacaoDTO postVacinacao(@RequestBody VacinacaoDTO vacinacao) {
        return vacinacaoService.criarVacinacao(vacinacao);
    }

    @DeleteMapping("/{vacinacaoId}")
    public String deleteVacinacao(@PathVariable Long vacinacaoId) {
        return vacinacaoService.excluirVacinacaoPorId(vacinacaoId);
    }

    @PutMapping("/{vacinacaoId}")
    public VacinacaoDTO putVacinacao(@PathVariable Long vacinacaoId, @RequestBody VacinacaoDTO dto) {
        return vacinacaoService.atualizarVacinacaoPorId(vacinacaoId, dto);
    }
}
