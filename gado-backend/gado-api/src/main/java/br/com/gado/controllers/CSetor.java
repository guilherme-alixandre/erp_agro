package br.com.gado.controllers;

import br.com.gado.application.services.SSetor;
import br.com.gado.dto.SetorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/setores")
public class CSetor {

    @Autowired
    private SSetor setorService;

    @GetMapping("/{id}")
    public Map<String, Object> getSetor(@PathVariable Long id){
        return setorService.procuraPorId(id);
    }

    @PostMapping("/")
    public String postSetor(@RequestBody SetorDto dto){
        return setorService.cadastra(dto);
    }

    @DeleteMapping("/{id}")
    public String deleteSetor(@PathVariable Long id){
        return setorService.deleta(id);
    }

    @PutMapping("/{id}")
    public String putSetor(@PathVariable Long id,
                           @RequestBody SetorDto dto)
    {
        return setorService.altera(id, dto);
    }

}
