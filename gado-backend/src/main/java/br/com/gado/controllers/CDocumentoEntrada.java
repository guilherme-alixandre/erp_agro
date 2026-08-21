package br.com.gado.controllers;

import br.com.gado.dto.documentoEntradaDto.DocumentoEntradaItemRespostaDto;
import br.com.gado.dto.documentoEntradaDto.DocumentoEntradaRespostaDto;
import br.com.gado.dto.documentoEntradaDto.NfeUpdateDto;
import br.com.gado.dto.documentoEntradaDto.ReciboSimplesCadastroDto;
import br.com.gado.dto.documentoEntradaDto.RecusaDocumentoDto;
import br.com.gado.dto.documentoEntradaDto.VincularProdutoDto;
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
 * Documentos de Entrada (NF-e e não fiscais). Como o restante do projeto, a identidade de quem
 * chama vem do header X-Usuario-Email (ver CConsumoEstoque) — a permissão de fato é validada em
 * SDocumentoEntrada, comparando o perfil do usuário contra a regra de cada operação.
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/documentos-entrada")
public class CDocumentoEntrada {

    @Autowired
    private SDocumentoEntrada documentoEntradaService;

    @GetMapping
    public List<DocumentoEntradaRespostaDto> listarTodos(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return documentoEntradaService.listarTodos(emailUsuario);
    }

    @GetMapping("/{id}")
    public DocumentoEntradaRespostaDto buscarPorId(
            @PathVariable Long id,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return documentoEntradaService.buscarPorId(id, emailUsuario);
    }

    @GetMapping("/pendentes")
    public List<DocumentoEntradaRespostaDto> listarPendentesAprovacao(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return documentoEntradaService.listarPendentesAprovacao(emailUsuario);
    }

    @PostMapping(value = "/importar-nfe", consumes = "multipart/form-data")
    public ResponseEntity<DocumentoEntradaRespostaDto> importarNfeXml(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) throws IOException {
        DocumentoEntradaRespostaDto criado = documentoEntradaService.importarNfeXml(file, emailUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PostMapping("/recibos")
    public ResponseEntity<DocumentoEntradaRespostaDto> cadastrarReciboSimples(
            @Valid @RequestBody ReciboSimplesCadastroDto dto,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        DocumentoEntradaRespostaDto criado = documentoEntradaService.cadastrarReciboSimples(dto, emailUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PostMapping("/{id}/aprovar")
    public DocumentoEntradaRespostaDto aprovarDocumento(
            @PathVariable Long id,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return documentoEntradaService.aprovarDocumento(id, emailUsuario);
    }

    @PostMapping("/{id}/recusar")
    public DocumentoEntradaRespostaDto recusarDocumento(
            @PathVariable Long id,
            @Valid @RequestBody RecusaDocumentoDto dto,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return documentoEntradaService.recusarDocumento(id, dto, emailUsuario);
    }

    /** dto.senhaConfirmacao carrega a senha de confirmação exigida pela dupla validação. */
    @PutMapping("/{id}/nfe")
    public DocumentoEntradaRespostaDto editarNfe(
            @PathVariable Long id,
            @Valid @RequestBody NfeUpdateDto dto,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return documentoEntradaService.editarNfe(id, dto, dto.getSenhaConfirmacao(), emailUsuario);
    }

    @PostMapping("/itens/{idItem}/vincular")
    public DocumentoEntradaItemRespostaDto vincularProduto(
            @PathVariable Long idItem,
            @Valid @RequestBody VincularProdutoDto dto,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return documentoEntradaService.vincularProduto(idItem, dto, emailUsuario);
    }
}
