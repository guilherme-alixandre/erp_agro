package br.com.gado.controllers;

import br.com.gado.application.dto.OcorrenciaAnimalDTO;
import br.com.gado.application.services.SOcorrenciaAnimal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ocorrenciaAnimal")
public class COcorrenciaAnimal {

    private final SOcorrenciaAnimal ocorrenciaAnimalService;

    public COcorrenciaAnimal(SOcorrenciaAnimal ocorrenciaAnimalService) {
        this.ocorrenciaAnimalService = ocorrenciaAnimalService;
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
