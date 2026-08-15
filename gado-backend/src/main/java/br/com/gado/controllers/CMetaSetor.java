package br.com.gado.controllers;

import br.com.gado.dto.metaSetorDto.MedicaoMetaCadastroDto;
import br.com.gado.dto.metaSetorDto.MedicaoMetaPutDto;
import br.com.gado.dto.metaSetorDto.MetaSetorCadastroDto;
import br.com.gado.dto.metaSetorDto.MetaSetorPutDto;
import br.com.gado.dto.metaSetorDto.MetaSetorRespostaDto;
import br.com.gado.services.SMetaSetor;
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
@RequestMapping("/api/metas-setor")
public class CMetaSetor {

    @Autowired
    private SMetaSetor metaSetorService;

    @Autowired
    private SPdfRelatorio pdfService;

    // ── MetaSetor ─────────────────────────────────────────────────────────

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getPdfMetas(@RequestParam(name = "setorId") Long setorId) {
        byte[] pdf = pdfService.gerarRelatorioMetas(setorId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"relatorio-metas.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping
    public List<MetaSetorRespostaDto> listarPorSetor(@RequestParam(name = "setorId") Long setorId) {
        return metaSetorService.listarPorSetor(setorId);
    }

    @GetMapping("/{id}")
    public MetaSetorRespostaDto buscarPorId(@PathVariable Long id) {
        return metaSetorService.buscarPorId(id);
    }

    @PostMapping
    public String cadastrar(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody MetaSetorCadastroDto dto) {
        metaSetorService.validaAdminOuGerente(emailUsuario);
        return metaSetorService.cadastrar(dto);
    }

    @PutMapping("/{id}")
    public String alterar(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long id,
            @Valid @RequestBody MetaSetorPutDto dto) {
        metaSetorService.validaAdminOuGerente(emailUsuario);
        return metaSetorService.alterar(id, dto);
    }

    @DeleteMapping("/{id}")
    public String deletar(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long id) {
        metaSetorService.validaAdminOuGerente(emailUsuario);
        return metaSetorService.deletar(id);
    }

    // ── MedicaoMeta ───────────────────────────────────────────────────────

    @PostMapping("/medicoes")
    public String cadastrarMedicao(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody MedicaoMetaCadastroDto dto) {
        metaSetorService.validaUsuarioAtivo(emailUsuario);
        return metaSetorService.cadastrarMedicao(dto, emailUsuario);
    }

    @PutMapping("/medicoes/{medicaoId}")
    public String atualizarMedicao(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long medicaoId,
            @Valid @RequestBody MedicaoMetaPutDto dto) {
        return metaSetorService.validarEAtualizarMedicao(medicaoId, dto, emailUsuario);
    }

    @DeleteMapping("/medicoes/{medicaoId}")
    public String deletarMedicao(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long medicaoId) {
        return metaSetorService.validarEDeletarMedicao(medicaoId, emailUsuario);
    }
}
