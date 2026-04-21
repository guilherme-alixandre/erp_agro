package br.com.gado.controllers;

import br.com.gado.application.dto.AnimalDto;
import br.com.gado.application.services.SAnimal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/animais")
public class CAnimal {

    private final SAnimal animalService;

    public CAnimal(SAnimal animalService) {
        this.animalService = animalService;
    }

    @GetMapping("/{brinco}")
    public AnimalDto getAnimal(@PathVariable String brinco) {
        return animalService.buscarPorBrinco(brinco);
    }

    @PostMapping("/usuarios/{email}")
    public AnimalDto postAnimal(@PathVariable String email, @RequestBody AnimalDto animal) {
        return animalService.cadastraAnimal(email, animal);
    }

    @DeleteMapping("/{brinco}")
    public String deleteAnimal(@PathVariable String brinco) {
        return animalService.deletaAnimal(brinco);
    }

    @PutMapping("/{brinco}")
    public AnimalDto putAnimal(@PathVariable String brinco, @RequestBody AnimalDto animal) {
        return animalService.alteraAnimal(brinco, animal);
    }
}
