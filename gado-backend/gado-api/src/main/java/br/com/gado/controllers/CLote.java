package br.com.gado.controllers;

import br.com.gado.application.services.SLote;
import br.com.gado.dto.loteDto.LoteCadastroDto;
import br.com.gado.dto.loteDto.LotePutDto;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lotes")
public class CLote {

    private final SLote loteService;

    public CLote(SLote loteService){
        this.loteService = loteService;
    }


    @GetMapping("/{id}")
    public Map<String, Object> getLote(@PathVariable Long id){
        return loteService.buscaPorid(id);
    }

    @PostMapping("/")
    public String postLote(@RequestBody LoteCadastroDto dto){
        return loteService.cadastra(dto);
    }

    @DeleteMapping("/{id}")
    public String deleteLote(@PathVariable Long id){
        return loteService.deleta(id);
    }

    @PutMapping("/{id}")
    public String putLote(@PathVariable Long id,
                          @RequestBody LotePutDto dto)
    {
        return loteService.altera(id, dto);
    }

}
