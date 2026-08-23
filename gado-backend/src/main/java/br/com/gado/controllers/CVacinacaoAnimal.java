package br.com.gado.controllers;

import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalCadastroDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalCancelamentoDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalEdicaoDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalResumoItemDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalRespostaDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.SVacinacaoAnimal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * "Vacinar Animais": aplicação de um insumo em um ou mais animais (ou em um
 * lote inteiro), com baixa automática de estoque. Aberto a qualquer usuário
 * ativo — ver SVacinacaoAnimal.validaUsuarioAtivo.
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/vacinacoes-animal")
public class CVacinacaoAnimal {

    @Autowired
    private SVacinacaoAnimal vacinacaoAnimalService;

    @GetMapping
    public List<VacinacaoAnimalRespostaDto> listar(
            @RequestParam(required = false) Long animalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        if (animalId != null) {
            return vacinacaoAnimalService.listarPorAnimal(animalId);
        }
        return vacinacaoAnimalService.listarTodos(dataInicio, dataFim);
    }

    @GetMapping("/resumo")
    public List<VacinacaoAnimalResumoItemDto> resumoPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return vacinacaoAnimalService.resumoPorPeriodo(dataInicio, dataFim);
    }

    @PostMapping
    public ResponseEntity<VacinacaoAnimalRespostaDto> registrarAplicacao(
            @Valid @RequestBody VacinacaoAnimalCadastroDto dto) {
        String emailUsuario = SecurityUtils.currentUserEmail();
        vacinacaoAnimalService.validaUsuarioAtivo(emailUsuario);
        VacinacaoAnimalRespostaDto criado = vacinacaoAnimalService.registrarAplicacao(dto, emailUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VacinacaoAnimalRespostaDto> editarAplicacao(
            @PathVariable Long id,
            @Valid @RequestBody VacinacaoAnimalEdicaoDto dto) {
        String emailUsuario = SecurityUtils.currentUserEmail();
        return ResponseEntity.ok(vacinacaoAnimalService.editarAplicacao(id, dto, emailUsuario));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<VacinacaoAnimalRespostaDto> cancelarAplicacao(
            @PathVariable Long id,
            @Valid @RequestBody VacinacaoAnimalCancelamentoDto dto) {
        String emailUsuario = SecurityUtils.currentUserEmail();
        vacinacaoAnimalService.validaUsuarioAtivo(emailUsuario);
        VacinacaoAnimalRespostaDto cancelado = vacinacaoAnimalService.cancelarAplicacao(id, dto, emailUsuario);
        return ResponseEntity.ok(cancelado);
    }
}
