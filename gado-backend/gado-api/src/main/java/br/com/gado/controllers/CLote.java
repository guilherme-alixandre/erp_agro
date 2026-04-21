package br.com.gado.controllers;

import br.com.gado.application.dto.loteDto.LoteCadastroDto;
import br.com.gado.application.dto.loteDto.LoteDto;
import br.com.gado.application.dto.loteDto.LotePutDto;
import br.com.gado.application.services.SLote;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lotes")
public class CLote {

    private final SLote loteService;

    public CLote(SLote loteService) {
        this.loteService = loteService;
    }

    @GetMapping("/{id}")
    public LoteDto getLote(@PathVariable Long id) {
        return loteService.buscaPorId(id);
    }

    @PostMapping("/")
    public LoteDto postLote(@RequestBody LoteCadastroDto dto) {
        return loteService.cadastra(dto);
    }

    @DeleteMapping("/{id}")
    public String deleteLote(@PathVariable Long id) {
        return loteService.deleta(id);
    }

    @PutMapping("/{id}")
    public LoteDto putLote(@PathVariable Long id, @RequestBody LotePutDto dto) {
        return loteService.altera(id, dto);
    }
}
