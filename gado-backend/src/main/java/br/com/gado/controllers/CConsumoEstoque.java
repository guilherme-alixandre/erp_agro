package br.com.gado.controllers;

import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueCadastroDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueCancelamentoDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueRespostaDto;
import br.com.gado.services.SConsumoEstoque;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public List<ConsumoEstoqueRespostaDto> listarTodos() {
        return consumoEstoqueService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<ConsumoEstoqueRespostaDto> registrarConsumo(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody ConsumoEstoqueCadastroDto dto) {
        consumoEstoqueService.validaUsuarioAtivo(emailUsuario);
        ConsumoEstoqueRespostaDto criado = consumoEstoqueService.registrarConsumo(dto, emailUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ConsumoEstoqueRespostaDto> cancelarConsumo(
            @PathVariable Long id,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody ConsumoEstoqueCancelamentoDto dto) {
        consumoEstoqueService.validaUsuarioAtivo(emailUsuario);
        ConsumoEstoqueRespostaDto cancelado = consumoEstoqueService.cancelarConsumo(id, dto, emailUsuario);
        return ResponseEntity.ok(cancelado);
    }
}
