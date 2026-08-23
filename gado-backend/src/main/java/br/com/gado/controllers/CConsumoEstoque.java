package br.com.gado.controllers;

import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueCadastroDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueCancelamentoDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueEdicaoDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueResumoItemDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueRespostaDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.SConsumoEstoque;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * "Consumo de Estoque": baixa de insumos do próprio estoque, aberta a
 * qualquer usuário ativo, com motivo/finalidade obrigatório. O cancelamento
 * (com justificativa obrigatória) é restrito ao autor da movimentação ou a
 * Administradores — ver SConsumoEstoque.validaPermissaoCancelamento.
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/consumo-estoque")
public class CConsumoEstoque {

    @Autowired
    private SConsumoEstoque consumoEstoqueService;

    @GetMapping
    public List<ConsumoEstoqueRespostaDto> listarTodos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return consumoEstoqueService.listarTodos(dataInicio, dataFim);
    }

    @GetMapping("/resumo")
    public List<ConsumoEstoqueResumoItemDto> resumoPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return consumoEstoqueService.resumoPorPeriodo(dataInicio, dataFim);
    }

    @PostMapping
    public ResponseEntity<ConsumoEstoqueRespostaDto> registrarConsumo(@Valid @RequestBody ConsumoEstoqueCadastroDto dto) {
        String emailUsuario = SecurityUtils.currentUserEmail();
        consumoEstoqueService.validaUsuarioAtivo(emailUsuario);
        ConsumoEstoqueRespostaDto criado = consumoEstoqueService.registrarConsumo(dto, emailUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsumoEstoqueRespostaDto> editarConsumo(@PathVariable Long id, @Valid @RequestBody ConsumoEstoqueEdicaoDto dto) {
        return ResponseEntity.ok(consumoEstoqueService.editarConsumo(id, dto, SecurityUtils.currentUserEmail()));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ConsumoEstoqueRespostaDto> cancelarConsumo(
            @PathVariable Long id,
            @Valid @RequestBody ConsumoEstoqueCancelamentoDto dto) {
        String emailUsuario = SecurityUtils.currentUserEmail();
        consumoEstoqueService.validaUsuarioAtivo(emailUsuario);
        ConsumoEstoqueRespostaDto cancelado = consumoEstoqueService.cancelarConsumo(id, dto, emailUsuario);
        return ResponseEntity.ok(cancelado);
    }
}
