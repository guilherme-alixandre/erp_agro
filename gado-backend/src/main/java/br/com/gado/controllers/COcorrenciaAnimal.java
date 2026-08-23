package br.com.gado.controllers;

import br.com.gado.dto.OcorrenciaAnimalDTO;
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

    @GetMapping("/{ocorrenciaAnimalId}")
    public OcorrenciaAnimalDTO getMovimentacaoEsotque(@PathVariable Long ocorrenciaAnimalId) {
        return ocorrenciaAnimalService.encontrarOcorrenciaAnimalPorId(ocorrenciaAnimalId);
    }

    @PostMapping("/")
    public OcorrenciaAnimalDTO postMovimentacaoEsotque(@RequestBody OcorrenciaAnimalDTO ocorrenciaAnimalId) {
        return ocorrenciaAnimalService.criarOcorrenciaAnimal(ocorrenciaAnimalId);
    }

    @DeleteMapping("/{ocorrenciaAnimalId}")
    public String deleteMovimentacaoEsotque(@PathVariable Long ocorrenciaAnimalId) {
        return ocorrenciaAnimalService.excluirOcorrenciaAnimal(ocorrenciaAnimalId);
    }

    @PutMapping("/{ocorrenciaAnimalId}")
    public OcorrenciaAnimalDTO putMovimentacaoEsotque(@PathVariable Long ocorrenciaAnimalId, @RequestBody OcorrenciaAnimalDTO dto) {
        return ocorrenciaAnimalService.atualizarOcorrenciaAnimal(ocorrenciaAnimalId, dto);
    }
}
