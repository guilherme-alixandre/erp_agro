package br.com.gado.controllers;

import br.com.gado.dto.TrasacaoDTO;
import br.com.gado.services.STrasacao;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/transacao")
public class CTransacao {

    private final STrasacao trasacaoService;

    public CTransacao(STrasacao trasacaoService) {
        this.trasacaoService = trasacaoService;
    }

    @GetMapping("/{transacaoId}")
    public TrasacaoDTO getMovimentacaoEsotque(@PathVariable Long transacaoId) {
        return trasacaoService.buscarTrasacaoPorId(transacaoId);
    }

    @PostMapping("/")
    public TrasacaoDTO postMovimentacaoEstoque(@RequestBody TrasacaoDTO transacaoId) {
        return trasacaoService.criarTrasacao(transacaoId);
    }

    @DeleteMapping("/{transacaoId}")
    public String deleteMovimentacaoEsotque(@PathVariable Long transacaoId) {
        return trasacaoService.excluirTrasacaoPorId(transacaoId);
    }

    @PutMapping("/{transacaoId}")
    public TrasacaoDTO putMovimentacaoEstoque(@PathVariable Long transacaoId, @RequestBody TrasacaoDTO dto) {
        return trasacaoService.atualizarTrasacaoPorId(transacaoId, dto);
    }
}
