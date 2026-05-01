package br.com.gado.controllers;

import br.com.gado.application.services.SAnimal;
import br.com.gado.application.dto.AnimalDto;
import br.com.gado.application.dto.AnimalRespostaDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/animais")
public class CAnimal {

    private final SAnimal animalService;

    public CAnimal(SAnimal animalService){
        this.animalService = animalService;
    }


    @GetMapping
    public List<AnimalRespostaDto> buscarAnimais(
            @RequestParam(name = "busca", required = false, defaultValue = "") String busca){
        return animalService.buscarPorTermo(busca);
    }

    @GetMapping("/{brinco}")
    public Map<String, Object> getAnimal(@PathVariable String brinco){
        return animalService.buscarPorBrinco(brinco);
    }

    @PostMapping("/usuarios/{email}")
    public String postAnimal(@PathVariable String email,
                             @RequestBody AnimalDto animal)
    {
        return animalService.cadastraAnimal(email, animal);
    }

    @DeleteMapping("/{brinco}")
    public String deleteAnimal(@PathVariable String brinco){
        return animalService.deletaAnimal(brinco);
    }

    @PutMapping("/{brinco}")
    public String putAnimal(@PathVariable String brinco,
                            @RequestBody AnimalDto animal)
    {
        return animalService.alteraAnimal(brinco, animal);
    }

}
