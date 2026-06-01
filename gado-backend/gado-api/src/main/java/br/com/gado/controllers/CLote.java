package br.com.gado.controllers;

import br.com.gado.application.services.SLote;
import br.com.gado.application.dto.loteDto.LoteCadastroDto;
import br.com.gado.application.dto.loteDto.LotePutDto;
import org.springframework.web.bind.annotation.*;

import br.com.gado.application.dto.loteDto.LoteDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lotes")
public class CLote {

    private final SLote loteService;

    public CLote(SLote loteService){
        this.loteService = loteService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<LoteDto> getLote(@PathVariable Long id) {
        LoteDto lote = loteService.buscaPorid(id);
        return ResponseEntity.ok(lote);
    }

    @GetMapping
    public ResponseEntity<List<LoteDto>> getLotes(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String descricao) {
        List<LoteDto> lotes = loteService.buscaLotes(id, descricao);
        return ResponseEntity.ok(lotes);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportLotesToCsv() {
        String csvContent = loteService.exportAllLotesToCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("filename", "lotes.csv");
        return new ResponseEntity<>(csvContent.getBytes(), headers, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<Map<String, Object>> postLote(@RequestBody LoteCadastroDto dto) {
        Map<String, Object> response = loteService.cadastra(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteLote(@PathVariable Long id) {
        loteService.deleta(id);
        return new ResponseEntity<>(Map.of("message", "Lote excluído com sucesso!"), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> putLote(@PathVariable Long id,
                                                       @RequestBody LotePutDto dto) {
        Map<String, Object> response = loteService.altera(id, dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
