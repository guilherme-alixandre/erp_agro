package br.com.gado.controllers;

import br.com.gado.application.dto.SetorDto;
import br.com.gado.application.services.SSetor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setores")
public class CSetor {

    @Autowired
    private SSetor setorService;

    @GetMapping("/{id}")
    public ResponseEntity<SetorDto> getSetor(@PathVariable Long id) {
        return ResponseEntity.ok(setorService.procuraPorId(id));
    }

    @PostMapping
    public ResponseEntity<SetorDto> postSetor(@RequestBody SetorDto dto) {
        SetorDto criado = setorService.cadastra(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSetor(@PathVariable Long id) {
        try {
            setorService.deleta(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SetorDto> putSetor(@PathVariable Long id, @RequestBody SetorDto dto) {
        return ResponseEntity.ok(setorService.altera(id, dto));
    }
}