package br.com.gado.controllers;

import br.com.gado.dto.unidadeMedidaDto.UnidadeMedidaCadastroDto;
import br.com.gado.dto.unidadeMedidaDto.UnidadeMedidaPutDto;
import br.com.gado.dto.unidadeMedidaDto.UnidadeMedidaRespostaDto;
import br.com.gado.services.SUnidadeMedida;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/unidades-medida")
public class CUnidadeMedida {

    @Autowired
    private SUnidadeMedida unidadeMedidaService;

    @GetMapping
    public List<UnidadeMedidaRespostaDto> getUnidades(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status) {
        return unidadeMedidaService.listar(busca, status);
    }

    @GetMapping("/{id}")
    public UnidadeMedidaRespostaDto getUnidadePorId(@PathVariable Long id) {
        return unidadeMedidaService.buscarPorId(id);
    }

    @PostMapping
    public UnidadeMedidaRespostaDto postUnidade(@Valid @RequestBody UnidadeMedidaCadastroDto dto) {
        return unidadeMedidaService.criar(dto);
    }

    @PutMapping("/{id}")
    public UnidadeMedidaRespostaDto putUnidade(@PathVariable Long id, @Valid @RequestBody UnidadeMedidaPutDto dto) {
        return unidadeMedidaService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public String deleteUnidade(@PathVariable Long id) {
        return unidadeMedidaService.inativar(id);
    }

    @PutMapping("/{id}/reativar")
    public String reativarUnidade(@PathVariable Long id) {
        return unidadeMedidaService.reativar(id);
    }
}
