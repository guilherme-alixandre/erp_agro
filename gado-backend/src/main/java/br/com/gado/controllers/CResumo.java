package br.com.gado.controllers;

import br.com.gado.dto.resumoDto.ResumoDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.SResumo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dashboard inicial — aberto a qualquer usuário autenticado; a visibilidade fina de cada bloco é decidida no service. */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/resumo")
public class CResumo {

    private final SResumo resumoService;

    public CResumo(SResumo resumoService) {
        this.resumoService = resumoService;
    }

    @GetMapping
    public ResumoDto getResumo() {
        return resumoService.gerarResumo(SecurityUtils.currentUserEmail());
    }
}
