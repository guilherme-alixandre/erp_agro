package br.com.gado.controllers;

import br.com.gado.dto.documentoEntradaDto.DocumentoEntradaItemRespostaDto;
import br.com.gado.dto.documentoEntradaDto.DocumentoEntradaRespostaDto;
import br.com.gado.dto.documentoEntradaDto.NfeUpdateDto;
import br.com.gado.dto.documentoEntradaDto.ReciboSimplesCadastroDto;
import br.com.gado.dto.documentoEntradaDto.RecusaDocumentoDto;
import br.com.gado.dto.documentoEntradaDto.VincularProdutoDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.SDocumentoEntrada;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Documentos de Entrada (NF-e e não fiscais). A identidade de quem chama vem do JWT autenticado
 * (SecurityUtils.currentUserEmail) — a permissão de fato é validada em SDocumentoEntrada,
 * comparando o perfil do usuário contra a regra de cada operação.
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/documentos-entrada")
public class CDocumentoEntrada {

    @Autowired
    private SDocumentoEntrada documentoEntradaService;

    @GetMapping
    public List<DocumentoEntradaRespostaDto> listarTodos() {
        return documentoEntradaService.listarTodos(SecurityUtils.currentUserEmail());
    }

    @GetMapping("/{id}")
    public DocumentoEntradaRespostaDto buscarPorId(@PathVariable Long id) {
        return documentoEntradaService.buscarPorId(id, SecurityUtils.currentUserEmail());
    }

    @GetMapping("/pendentes")
    public List<DocumentoEntradaRespostaDto> listarPendentesAprovacao() {
        return documentoEntradaService.listarPendentesAprovacao(SecurityUtils.currentUserEmail());
    }

    @PostMapping(value = "/importar-nfe", consumes = "multipart/form-data")
    public ResponseEntity<DocumentoEntradaRespostaDto> importarNfeXml(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fornecedorId") Long fornecedorId) throws IOException {
        DocumentoEntradaRespostaDto criado = documentoEntradaService.importarNfeXml(file, fornecedorId, SecurityUtils.currentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PostMapping("/recibos")
    public ResponseEntity<DocumentoEntradaRespostaDto> cadastrarReciboSimples(@Valid @RequestBody ReciboSimplesCadastroDto dto) {
        DocumentoEntradaRespostaDto criado = documentoEntradaService.cadastrarReciboSimples(dto, SecurityUtils.currentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PostMapping("/{id}/aprovar")
    public DocumentoEntradaRespostaDto aprovarDocumento(@PathVariable Long id) {
        return documentoEntradaService.aprovarDocumento(id, SecurityUtils.currentUserEmail());
    }

    @PostMapping("/{id}/recusar")
    public DocumentoEntradaRespostaDto recusarDocumento(@PathVariable Long id, @Valid @RequestBody RecusaDocumentoDto dto) {
        return documentoEntradaService.recusarDocumento(id, dto, SecurityUtils.currentUserEmail());
    }

    /** dto.senhaConfirmacao carrega a senha de confirmação exigida pela dupla validação. */
    @PutMapping("/{id}/nfe")
    public DocumentoEntradaRespostaDto editarNfe(@PathVariable Long id, @Valid @RequestBody NfeUpdateDto dto) {
        return documentoEntradaService.editarNfe(id, dto, dto.getSenhaConfirmacao(), SecurityUtils.currentUserEmail());
    }

    @PostMapping("/itens/{idItem}/vincular")
    public DocumentoEntradaItemRespostaDto vincularProduto(@PathVariable Long idItem, @Valid @RequestBody VincularProdutoDto dto) {
        return documentoEntradaService.vincularProduto(idItem, dto, SecurityUtils.currentUserEmail());
    }

    @DeleteMapping("/{id}")
    public String excluirDocumento(@PathVariable Long id) {
        return documentoEntradaService.excluirDocumento(id, SecurityUtils.currentUserEmail());
    }
}
