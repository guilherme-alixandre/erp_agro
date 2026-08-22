package br.com.gado.controllers;

import br.com.gado.dto.loteDto.CustoRacaoLoteDto;
import br.com.gado.dto.loteDto.LoteCadastroDto;
import br.com.gado.dto.loteDto.LoteDto;
import br.com.gado.dto.loteDto.LotePutDto;
import br.com.gado.dto.loteDto.TransferenciaAnimalDto;
import br.com.gado.services.SLote;
import br.com.gado.services.SPdfRelatorio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/lotes")
public class CLote {

    @Autowired
    private SLote loteService;

    @Autowired
    private SPdfRelatorio pdfService;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getPdfLotes() {
        byte[] pdf = pdfService.gerarRelatorioLotes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"relatorio-lotes.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}")
    public LoteDto getLote(@PathVariable Long id) {
        return loteService.buscaPorId(id);
    }

    @GetMapping("/{id}/custo-racao")
    public CustoRacaoLoteDto getCustoRacao(@PathVariable Long id) {
        return loteService.calcularCustoRacaoAcumulado(id);
    }

    @GetMapping
    public List<LoteDto> getLotes() {
        return loteService.listarTodos();
    }

    @PostMapping
    public String postLote(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody LoteCadastroDto dto) {
        return loteService.cadastra(emailUsuario, dto);
    }

    @PutMapping("/{id}")
    public String putLote(
            @PathVariable Long id,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody LotePutDto dto) {
        return loteService.altera(id, emailUsuario, dto);
    }

    @DeleteMapping("/{id}")
    public String deleteLote(
            @PathVariable Long id,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return loteService.deleta(id, emailUsuario);
    }

    @PostMapping("/transferir-animal")
    public String transferirAnimal(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody TransferenciaAnimalDto dto) {
        return loteService.transferirAnimal(emailUsuario, dto);
    }
}
