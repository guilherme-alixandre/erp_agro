package br.com.gado.controllers;

import br.com.gado.application.dto.SetorDto;
import br.com.gado.application.services.SSetor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setores")
public class CSetor {

    @Autowired
    private SSetor setorService;

    @GetMapping("/{id}")
    public SetorDto getSetor(@PathVariable Long id) {
        return setorService.procuraPorId(id);
    }

    @PostMapping("/")
    public SetorDto postSetor(@RequestBody SetorDto dto) {
        return setorService.cadastra(dto);
    }

    @DeleteMapping("/{id}")
    public String deleteSetor(@PathVariable Long id) {
        return setorService.deleta(id);
    }

    @PutMapping("/{id}")
    public SetorDto putSetor(@PathVariable Long id, @RequestBody SetorDto dto) {
        return setorService.altera(id, dto);
    }
}
