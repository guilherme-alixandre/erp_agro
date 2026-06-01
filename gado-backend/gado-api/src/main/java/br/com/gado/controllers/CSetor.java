package br.com.gado.controllers;

import br.com.gado.application.services.SSetor;
import br.com.gado.application.dto.setorDto.SetorCadastroDto;
import br.com.gado.application.dto.setorDto.SetorDto;
import br.com.gado.application.dto.setorDto.SetorPutDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/setores")
public class CSetor {

    @Autowired
    private SSetor setorService;

    @GetMapping("/{id}")
    public ResponseEntity<SetorDto> getSetor(@PathVariable Long id) {
        SetorDto setor = setorService.buscaPorId(id);
        return ResponseEntity.ok(setor);
    }

    @GetMapping
    public ResponseEntity<Page<SetorDto>> getSetores(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String descricao,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SetorDto> setores = setorService.buscaSetores(id, descricao, pageable);
        return ResponseEntity.ok(setores);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportSetoresToCsv() {
        String csvContent = setorService.exportAllSetoresToCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("filename", "setores.csv");
        return new ResponseEntity<>(csvContent.getBytes(), headers, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<Map<String, Object>> postSetor(@RequestBody SetorCadastroDto dto) {
        Map<String, Object> response = setorService.cadastra(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSetor(@PathVariable Long id) {
        Map<String, String> response = setorService.deleta(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> putSetor(@PathVariable Long id,
                                                       @RequestBody SetorPutDto dto) {
        Map<String, Object> response = setorService.altera(id, dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
