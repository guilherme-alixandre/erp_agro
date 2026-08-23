package br.com.gado.controllers;

import br.com.gado.dto.racaDto.RacaCadastroDto;
import br.com.gado.dto.racaDto.RacaPutDto;
import br.com.gado.dto.racaDto.RacaRespostaDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.SRaca;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/racas")
public class CRaca {

    @Autowired
    private SRaca racaService;

    @GetMapping
    public List<RacaRespostaDto> getRacas(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status) {
        return racaService.listar(busca, status);
    }

    @GetMapping("/{id}")
    public RacaRespostaDto getRacaPorId(@PathVariable Long id) {
        return racaService.buscarPorId(id);
    }

    @PostMapping
    public RacaRespostaDto postRaca(@Valid @RequestBody RacaCadastroDto dto) {
        return racaService.criar(dto, SecurityUtils.currentUserEmail());
    }

    @PutMapping("/{id}")
    public RacaRespostaDto putRaca(@PathVariable Long id, @Valid @RequestBody RacaPutDto dto) {
        return racaService.atualizar(id, dto, SecurityUtils.currentUserEmail());
    }

    @DeleteMapping("/{id}")
    public String deleteRaca(@PathVariable Long id) {
        return racaService.inativar(id, SecurityUtils.currentUserEmail());
    }

    @PutMapping("/{id}/reativar")
    public String reativarRaca(@PathVariable Long id) {
        return racaService.reativar(id, SecurityUtils.currentUserEmail());
    }
}
