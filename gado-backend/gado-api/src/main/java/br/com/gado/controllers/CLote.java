package br.com.gado.controllers;

import br.com.gado.application.dto.loteDto.LoteCadastroDto;
import br.com.gado.application.dto.loteDto.LotePutDto;
import br.com.gado.application.dto.loteDto.LoteRespostaDto;
import br.com.gado.application.dto.loteDto.TransferenciaAnimalDto;
import br.com.gado.application.services.SLote;
import br.com.gado.application.services.SPdfRelatorio;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lotes")
public class CLote {

    private final SLote loteService;
    private final SPdfRelatorio pdfService;

    public CLote(SLote loteService, SPdfRelatorio pdfService) {
        this.loteService = loteService;
        this.pdfService = pdfService;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getPdfLotes() {
        byte[] pdf = pdfService.gerarRelatorioLotes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"relatorio-lotes.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Lista todos os lotes ativos.
     * GET /api/lotes
     */
    @GetMapping
    public List<LoteRespostaDto> getLotes() {
        return loteService.listarTodos();
    }

    /**
     * Busca um lote ativo pelo ID, com todas as alocações e animais.
     * GET /api/lotes/{id}
     */
    @GetMapping("/{id}")
    public LoteRespostaDto getLote(@PathVariable Long id) {
        return loteService.buscaPorid(id);
    }

    /**
     * Cadastra um novo lote com alocações em setores.
     * O e-mail do responsável é obrigatório e enviado no header X-Usuario-Email.
     * POST /api/lotes
     */
    @PostMapping
    public String postLote(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody LoteCadastroDto dto) {
        return loteService.cadastra(emailUsuario, dto);
    }

    /**
     * Atualiza corBrinco e/ou redistribui as alocações de um lote ativo.
     * O código (LOTxxx) e o criadoPor são imutáveis.
     * PUT /api/lotes/{id}
     */
    @PutMapping("/{id}")
    public String putLote(
            @PathVariable Long id,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody LotePutDto dto) {
        return loteService.altera(id, emailUsuario, dto);
    }

    /**
     * Remove o lote:
     *  - Hard delete se não houver vínculos com metas ou movimentações.
     *  - Soft delete (inativação) caso contrário.
     * DELETE /api/lotes/{id}
     */
    @DeleteMapping("/{id}")
    public String deleteLote(
            @PathVariable Long id,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return loteService.deleta(id, emailUsuario);
    }

    /**
     * Retorna (ou cria) a alocação do lote padrão no setor informado.
     * Usado pelo frontend quando o usuário cadastra um animal no lote padrão.
     * GET /api/lotes/padrao/alocar/{setorId}
     */
    @GetMapping("/padrao/alocar/{setorId}")
    public ResponseEntity<Map<String, Long>> alocarSetorPadrao(
            @PathVariable Long setorId,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        Long loteSectorId = loteService.obterOuCriarAlocacaoPadrao(setorId);
        return ResponseEntity.ok(Map.of("loteSectorId", loteSectorId));
    }

    /**
     * Transfere um animal do seu lote/setor atual para o lote e setor informados.
     * Disponível apenas para Administradores, Gerentes e Cuidadores Chefe.
     * POST /api/lotes/transferir-animal
     */
    @PostMapping("/transferir-animal")
    public String transferirAnimal(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario,
            @Valid @RequestBody TransferenciaAnimalDto dto) {
        return loteService.transferirAnimal(emailUsuario, dto);
    }
}
