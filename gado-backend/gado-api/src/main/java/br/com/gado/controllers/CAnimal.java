package br.com.gado.controllers;

import br.com.gado.application.services.SAnimal;
import br.com.gado.dto.AnimalDto;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CAnimal {

    private final SAnimal animalService;

    public CAnimal(SAnimal animalService){
        this.animalService = animalService;
    }


    @GetMapping("/animais/{brinco}")
    public Map<String, Object> getAnimal(@PathVariable String brinco){
        return animalService.buscarPorBrinco(brinco);
    }

    @PostMapping("/animais")
    public String postAnimal(@RequestBody AnimalDto animal){
        return animalService.cadastraAnimal(animal);
    }

    @DeleteMapping("/animais/{brinco}")
    public String deleteAnimal(@PathVariable String brinco){
        return animalService.deletaAnimal(brinco);
    }

    @PutMapping("/animais/{brinco}")
    public String putAnimal(@PathVariable String brinco,
                            @RequestBody AnimalDto animal)
    {
        return animalService.alteraAnimal(brinco, animal);
    }

}
