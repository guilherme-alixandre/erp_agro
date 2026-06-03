package br.com.gado.controllers;

import br.com.gado.dto.MovimentacaoEstoqueDTO;
import br.com.gado.services.SMovimentacaoEstoque;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/movimentacaoEstoque")
public class CMovimentacaoEstoque {

    private final SMovimentacaoEstoque movimetacaoEstoqueService;

    public CMovimentacaoEstoque(SMovimentacaoEstoque movimetacaoEstoqueService) {
        this.movimetacaoEstoqueService = movimetacaoEstoqueService;
    }

    @GetMapping("/{movimentacaoEstoqueId}")
    public MovimentacaoEstoqueDTO getMovimentacaoEsotque(@PathVariable Long movimentacaoEstoqueId) {
        return movimetacaoEstoqueService.buscarMovimentacaoEstoquePorId(movimentacaoEstoqueId);
    }

    @PostMapping("/")
    public MovimentacaoEstoqueDTO postMovimentacaoEstoque(@RequestBody MovimentacaoEstoqueDTO movimentacaoEstoqueId) {
        return movimetacaoEstoqueService.criarMovimentacaoEstoque(movimentacaoEstoqueId);
    }

    @DeleteMapping("/{movimentacaoEstoqueId}")
    public String deleteMovimentacaoEsotque(@PathVariable Long movimentacaoEstoqueId) {
        return movimetacaoEstoqueService.excluirMovimentacaoEstoquePorId(movimentacaoEstoqueId);
    }

    @PutMapping("/{movimentacaoEstoqueId}")
    public MovimentacaoEstoqueDTO putMovimentacaoEstoque(@PathVariable Long movimentacaoEstoqueId, @RequestBody MovimentacaoEstoqueDTO dto) {
        return movimetacaoEstoqueService.atualizarMovimentacaoEstoquePorId(movimentacaoEstoqueId, dto);
    }
}
