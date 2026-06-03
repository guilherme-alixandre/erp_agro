package br.com.gado.controllers;

import br.com.gado.dto.loteDto.LoteCadastroDto;
import br.com.gado.dto.loteDto.LoteDto;
import br.com.gado.dto.loteDto.LotePutDto;
import br.com.gado.services.SLote;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
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
