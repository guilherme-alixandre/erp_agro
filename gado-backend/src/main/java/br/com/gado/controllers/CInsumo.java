package br.com.gado.controllers;

import br.com.gado.dto.insumoDto.EntradaEstoqueDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueCadastroDto;
import br.com.gado.dto.insumoDto.InsumoEstoquePutDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueRespostaDto;
import br.com.gado.services.SInsumo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/insumos")
public class CInsumo {

    @Autowired
    private SInsumo insumoService;

    // ── Estoque (Módulo de Insumos) ──────────────────────────────────────

    @GetMapping("/estoque")
    public List<InsumoEstoqueRespostaDto> getEstoque(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status) {
        return insumoService.listarEstoque(busca, status);
    }

    @GetMapping("/estoque/{id}")
    public InsumoEstoqueRespostaDto getEstoquePorId(@PathVariable Long id) {
        return insumoService.buscarEstoquePorId(id);
    }

    @PostMapping("/estoque")
    public InsumoEstoqueRespostaDto postInsumoEstoque(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody InsumoEstoqueCadastroDto dto) {
        return insumoService.criarInsumoEstoque(dto, emailUsuario);
    }

    @PutMapping("/estoque/{id}")
    public InsumoEstoqueRespostaDto putInsumoEstoque(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long id,
            @Valid @RequestBody InsumoEstoquePutDto dto) {
        return insumoService.atualizarDadosEstoque(id, dto, emailUsuario);
    }

    @PostMapping("/estoque/{id}/entradas")
    public InsumoEstoqueRespostaDto postEntradaEstoque(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long id,
            @Valid @RequestBody EntradaEstoqueDto dto) {
        return insumoService.registrarEntradaEstoque(id, dto, emailUsuario);
    }

    @DeleteMapping("/estoque/{id}")
    public String deleteInsumoEstoque(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long id) {
        return insumoService.inativarInsumo(id, emailUsuario);
    }

    @PutMapping("/estoque/{id}/reativar")
    public String reativarInsumoEstoque(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long id) {
        return insumoService.reativarInsumo(id, emailUsuario);
    }
}
