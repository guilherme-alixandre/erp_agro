package br.com.gado.controllers;

import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalAtualizacaoDto;
import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalCadastroDto;
import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalRespostaDto;
import br.com.gado.services.SOcorrenciaAnimal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/ocorrenciaAnimal")
public class COcorrenciaAnimal {

    private final SOcorrenciaAnimal ocorrenciaAnimalService;

    public COcorrenciaAnimal(SOcorrenciaAnimal ocorrenciaAnimalService) {
        this.ocorrenciaAnimalService = ocorrenciaAnimalService;
    }

    @GetMapping
    public List<OcorrenciaAnimalRespostaDto> listarPorAnimal(@RequestParam Long animalId) {
        return ocorrenciaAnimalService.listarPorAnimal(animalId);
    }

    @PostMapping
    public ResponseEntity<OcorrenciaAnimalRespostaDto> cadastrar(@Valid @RequestBody OcorrenciaAnimalCadastroDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ocorrenciaAnimalService.criarOcorrenciaPorAnimalId(dto));
    }

    @PutMapping("/{ocorrenciaAnimalId}")
    public OcorrenciaAnimalRespostaDto atualizar(
            @PathVariable Long ocorrenciaAnimalId,
            @Valid @RequestBody OcorrenciaAnimalAtualizacaoDto dto) {
        return ocorrenciaAnimalService.atualizarOcorrencia(ocorrenciaAnimalId, dto);
    }

    @DeleteMapping("/{ocorrenciaAnimalId}")
    public ResponseEntity<Void> excluir(@PathVariable Long ocorrenciaAnimalId) {
        ocorrenciaAnimalService.excluirOcorrencia(ocorrenciaAnimalId);
        return ResponseEntity.noContent().build();
    }
}
