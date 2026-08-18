package br.com.gado.controllers;

import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoCadastroDto;
import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoRespostaDto;
import br.com.gado.services.SConsumoInsumo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * "Alimentar Lote": registro de consumo diário de insumos por Setor.
 * Aberto a qualquer usuário ativo (incluindo Cuidadores comuns) — ver SConsumoInsumo.validaUsuarioAtivo.
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/consumos-insumo")
public class CConsumoInsumo {

    @Autowired
    private SConsumoInsumo consumoInsumoService;

    @GetMapping
    public List<ConsumoInsumoRespostaDto> listarPorSetor(@RequestParam(name = "setorId") Long setorId) {
        return consumoInsumoService.listarPorSetor(setorId);
    }

    @PostMapping
    public ResponseEntity<ConsumoInsumoRespostaDto> registrarConsumo(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody ConsumoInsumoCadastroDto dto) {
        consumoInsumoService.validaUsuarioAtivo(emailUsuario);
        ConsumoInsumoRespostaDto criado = consumoInsumoService.registrarConsumo(dto, emailUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }
}
