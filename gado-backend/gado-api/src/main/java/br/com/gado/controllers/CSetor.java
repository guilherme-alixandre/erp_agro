package br.com.gado.controllers;

import br.com.gado.application.dto.SetorDto;
import br.com.gado.application.services.SPdfRelatorio;
import br.com.gado.application.services.SSetor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/setores")
public class CSetor {

    @Autowired
    private SSetor setorService;

    @Autowired
    private SPdfRelatorio pdfService;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getPdfSetores() {
        byte[] pdf = pdfService.gerarRelatorioSetores();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"relatorio-setores.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SetorDto> getSetor(@PathVariable Long id) {
        return ResponseEntity.ok(setorService.procuraPorId(id));
    }

    @GetMapping
    public ResponseEntity<ArrayList<SetorDto>> getListaSetor() {
        return ResponseEntity.ok(setorService.buscarTodos());
    }

    @PostMapping
    public ResponseEntity<SetorDto> postSetor(
            @RequestHeader(name = "X-Usuario-Email", required = false) String email,
            @RequestBody SetorDto dto) {
        SetorDto criado = setorService.cadastra(dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSetor(
            @RequestHeader(name = "X-Usuario-Email", required = false) String email,
            @PathVariable Long id) {
        try {
            setorService.deleta(id, email);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SetorDto> putSetor(
            @PathVariable Long id,
            @RequestHeader(name = "X-Usuario-Email", required = false) String email,
            @RequestBody SetorDto dto) {
        return ResponseEntity.ok(setorService.altera(id, dto, email));
    }
}
