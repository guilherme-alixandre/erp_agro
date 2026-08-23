package br.com.gado.controllers;

import br.com.gado.dto.SetorDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.SPdfRelatorio;
import br.com.gado.services.SSetor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
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
    public ResponseEntity<ArrayList<SetorDto>> getSetores() {
        return ResponseEntity.ok(setorService.buscarTodos());
    }

    @PostMapping
    public ResponseEntity<SetorDto> postSetor(@RequestBody SetorDto dto) {
        SetorDto criado = setorService.cadastra(dto, SecurityUtils.currentUserEmail());
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
        return ResponseEntity.ok(setorService.altera(id, dto, SecurityUtils.currentUserEmail()));
    }
}
