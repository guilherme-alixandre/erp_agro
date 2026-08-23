package br.com.gado.controllers;

import br.com.gado.dto.notaFiscalDto.NotaFiscalResumoDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.SNotaFiscal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Listagem unificada de Notas Fiscais (entradas + receitas) com filtros de número/chave/direção. */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/notas-fiscais")
public class CNotaFiscal {

    @Autowired
    private SNotaFiscal notaFiscalService;

    @GetMapping
    public List<NotaFiscalResumoDto> listar(
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) String chave,
            @RequestParam(required = false) String direcao) {
        return notaFiscalService.listar(numero, chave, direcao, SecurityUtils.currentUserEmail());
    }
}
