package br.com.gado.controllers;

import br.com.gado.dto.InsumoDto;
import br.com.gado.dto.insumoDto.EntradaEstoqueDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueCadastroDto;
import br.com.gado.dto.insumoDto.InsumoEstoquePutDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueRespostaDto;
import br.com.gado.dto.insumoDto.VacinaCadastroDto;
import br.com.gado.dto.insumoDto.VacinaPutDto;
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

    @GetMapping("/vacinas")
    public List<InsumoDto> getVacinas(@RequestParam(required = false) String busca) {
        return insumoService.listarVacinas(busca);
    }

    @PostMapping("/vacinas")
    public InsumoDto postVacina(@RequestBody VacinaCadastroDto dto) {
        return insumoService.criarVacina(dto);
    }

    @PutMapping("/vacinas/{id}")
    public InsumoDto putVacina(@PathVariable Long id, @RequestBody VacinaPutDto dto) {
        return insumoService.atualizarVacina(id, dto);
    }

    @DeleteMapping("/vacinas/{id}")
    public String deleteVacina(@PathVariable Long id) {
        return insumoService.deletarVacina(id);
    }

    @GetMapping("/{id}")
    public InsumoDto getInsumo(@PathVariable Long id) {
        return insumoService.buscaPorId(id);
    }

    @PostMapping("/")
    public InsumoDto postInsumo(@RequestBody InsumoDto dto) {
        return insumoService.cadastraInsumo(dto);
    }

    @DeleteMapping("/{id}")
    public String deleteInsumo(@PathVariable Long id) {
        return insumoService.deletaInsumo(id);
    }

    @PutMapping("/{id}")
    public InsumoDto putInsumo(@PathVariable Long id, @RequestBody InsumoDto dto) {
        return insumoService.alteraInsumo(id, dto);
    }

    // ── Estoque (Módulo de Insumos) ──────────────────────────────────────

    @GetMapping("/estoque")
    public List<InsumoEstoqueRespostaDto> getEstoque(@RequestParam(required = false) String busca) {
        return insumoService.listarEstoque(busca);
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
}
