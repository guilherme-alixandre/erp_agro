package br.com.gado.controllers;

import br.com.gado.application.services.SOcorrenciaAnimal;
import br.com.gado.dto.MovimentacaoEstoqueDTO;
import br.com.gado.dto.OcorrenciaAnimalDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ocorrenciaAnimal")
public class COcorrenciaAnimal {

    private final SOcorrenciaAnimal ocorrenciaAnimalService;

    public COcorrenciaAnimal(SOcorrenciaAnimal ocorrenciaAnimalService){
        this.ocorrenciaAnimalService = ocorrenciaAnimalService;
    }


    @GetMapping("/{ocorrenciaAnimalId}")
    public OcorrenciaAnimalDTO getMovimentacaoEsotque(@PathVariable OcorrenciaAnimalDTO ocorrenciaAnimalId){
        return ocorrenciaAnimalService.encontrarOcorrenciaAnimalPorId(ocorrenciaAnimalId);
    }

    @PostMapping("/")
    public OcorrenciaAnimalDTO postMovimentacaoEsotque(@RequestBody OcorrenciaAnimalDTO ocorrenciaAnimalId){
        return ocorrenciaAnimalService.criarOcorrenciaAnimal(ocorrenciaAnimalId);
    }

    @DeleteMapping("/{ocorrenciaAnimalId}")
    public Boolean deleteMovimentacaoEsotque(@PathVariable Long ocorrenciaAnimalId){

        return ocorrenciaAnimalService.excluirOcorrenciaAnimal(ocorrenciaAnimalId);
    }

    @PutMapping("/{ocorrenciaAnimalId}")
    public OcorrenciaAnimalDTO putMovimentacaoEsotque(@PathVariable OcorrenciaAnimalDTO ocorrenciaAnimalId){
        return ocorrenciaAnimalService.atualizarOcorrenciaAnimal(ocorrenciaAnimalId);
    }

}
