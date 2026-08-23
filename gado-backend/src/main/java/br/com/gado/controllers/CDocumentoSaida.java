package br.com.gado.controllers;

import br.com.gado.dto.documentoSaidaDto.DocumentoSaidaRespostaDto;
import br.com.gado.dto.documentoSaidaDto.VendaAnimalCadastroDto;
import br.com.gado.dto.documentoSaidaDto.VendaLeiteCadastroDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.SDocumentoSaida;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/documentos-saida")
public class CDocumentoSaida {

    @Autowired
    private SDocumentoSaida documentoSaidaService;

    @GetMapping
    public List<DocumentoSaidaRespostaDto> listarTodos() {
        return documentoSaidaService.listarTodos(SecurityUtils.currentUserEmail());
    }

    @PostMapping("/venda-leite")
    public ResponseEntity<DocumentoSaidaRespostaDto> cadastrarVendaLeite(@Valid @RequestBody VendaLeiteCadastroDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentoSaidaService.cadastrarVendaLeite(dto, SecurityUtils.currentUserEmail()));
    }

    @PostMapping("/venda-animal")
    public ResponseEntity<DocumentoSaidaRespostaDto> cadastrarVendaAnimal(@Valid @RequestBody VendaAnimalCadastroDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentoSaidaService.cadastrarVendaAnimal(dto, SecurityUtils.currentUserEmail()));
    }
}
