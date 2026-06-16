package br.com.gado.controllers;

import br.com.gado.application.dto.metaSetorDto.MedicaoMetaCadastroDto;
import br.com.gado.application.dto.metaSetorDto.MedicaoMetaPutDto;
import br.com.gado.application.dto.metaSetorDto.MetaSetorCadastroDto;
import br.com.gado.application.dto.metaSetorDto.MetaSetorPutDto;
import br.com.gado.application.dto.metaSetorDto.MetaSetorRespostaDto;
import br.com.gado.application.services.SMetaSetor;
import br.com.gado.application.services.SPdfRelatorio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metas-setor")
public class CMetaSetor {

    @Autowired
    private SMetaSetor metaSetorService;

    @Autowired
    private SPdfRelatorio pdfService;

    // ── MetaSetor ─────────────────────────────────────────────────────────────

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getPdfMetas(
            @RequestParam(name = "setorId") Long setorId) {
        byte[] pdf = pdfService.gerarRelatorioMetas(setorId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"relatorio-metas.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Lista todas as metas de um setor específico.
     * GET /api/metas-setor?setorId=1
     */
    @GetMapping
    public List<MetaSetorRespostaDto> listarPorSetor(
            @RequestParam(name = "setorId") Long setorId) {
        return metaSetorService.listarPorSetor(setorId);
    }

    /**
     * Busca uma meta pelo ID, já com progresso calculado.
     * GET /api/metas-setor/{id}
     */
    @GetMapping("/{id}")
    public MetaSetorRespostaDto buscarPorId(@PathVariable Long id) {
        return metaSetorService.buscarPorId(id);
    }

    /**
     * Cadastra uma nova meta para um setor.
     * Restrito a ADMINISTRADOR e GERENTE (via header X-Usuario-Email).
     * POST /api/metas-setor
     */
    @PostMapping
    public String cadastrar(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody MetaSetorCadastroDto dto) {
        metaSetorService.validaAdminOuGerente(emailUsuario);
        return metaSetorService.cadastrar(dto);
    }

    /**
     * Atualiza campos editáveis de uma meta existente.
     * Restrito a ADMINISTRADOR e GERENTE.
     * PUT /api/metas-setor/{id}
     */
    @PutMapping("/{id}")
    public String alterar(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long id,
            @Valid @RequestBody MetaSetorPutDto dto) {
        metaSetorService.validaAdminOuGerente(emailUsuario);
        return metaSetorService.alterar(id, dto);
    }

    /**
     * Remove uma meta e todas as suas medições (cascade).
     * Restrito a ADMINISTRADOR e GERENTE.
     * DELETE /api/metas-setor/{id}
     */
    @DeleteMapping("/{id}")
    public String deletar(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long id) {
        metaSetorService.validaAdminOuGerente(emailUsuario);
        return metaSetorService.deletar(id);
    }

    // ── MedicaoMeta ───────────────────────────────────────────────────────────

    /**
     * Cadastra uma nova medição em uma meta.
     * Permitido a qualquer usuário ativo.
     * POST /api/metas-setor/medicoes
     */
    @PostMapping("/medicoes")
    public String cadastrarMedicao(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody MedicaoMetaCadastroDto dto) {
        metaSetorService.validaQualquerPerfil(emailUsuario);
        return metaSetorService.cadastrarMedicao(dto, emailUsuario);
    }

    /**
     * Atualiza uma medição existente.
     * ADMINISTRADOR, GERENTE, CUIDADOR_CHEFE: podem editar qualquer medição.
     * CUIDADOR: só pode editar a própria.
     * PUT /api/metas-setor/medicoes/{medicaoId}
     */
    @PutMapping("/medicoes/{medicaoId}")
    public String atualizarMedicao(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long medicaoId,
            @Valid @RequestBody MedicaoMetaPutDto dto) {
        return metaSetorService.validarEAtualizarMedicao(medicaoId, dto, emailUsuario);
    }

    /**
     * Remove uma medição específica.
     * Restrito a ADMINISTRADOR e GERENTE.
     * DELETE /api/metas-setor/medicoes/{medicaoId}
     */
    @DeleteMapping("/medicoes/{medicaoId}")
    public String deletarMedicao(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @PathVariable Long medicaoId) {
        metaSetorService.validaAdminOuGerente(emailUsuario);
        return metaSetorService.deletarMedicao(medicaoId);
    }
}
