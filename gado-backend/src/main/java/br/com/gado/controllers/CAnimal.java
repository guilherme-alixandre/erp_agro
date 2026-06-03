package br.com.gado.controllers;

import br.com.gado.dto.AnimalDto;
import br.com.gado.services.SAnimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/animais")
public class CAnimal {

    @Autowired
    private SAnimal animalService;

    @GetMapping("/{brinco}")
    public ResponseEntity<AnimalDto> getAnimal(@PathVariable String brinco) {
        return ResponseEntity.ok(animalService.buscarPorBrinco(brinco));
    }

    @GetMapping
    public ResponseEntity<ArrayList<AnimalDto>> getAnimals(){
        return ResponseEntity.ok(animalService.buscarTodosAnimais());
    }

    @PostMapping("/usuarios/{email}")
    public ResponseEntity<AnimalDto> postAnimal(@PathVariable String email, @RequestBody AnimalDto animal) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(animalService.cadastraAnimal(email, animal));
    }

    @DeleteMapping("/{brinco}")
    public ResponseEntity<String> deleteAnimal(@PathVariable String brinco) {
        return ResponseEntity.ok(animalService.deletaAnimal(brinco));
    }

    @PutMapping("/{brinco}")
    public ResponseEntity<AnimalDto> putAnimal(@PathVariable String brinco, @RequestBody AnimalDto animal) {
        return ResponseEntity.ok(animalService.alteraAnimal(brinco, animal));
    }
}
