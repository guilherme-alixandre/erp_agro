package br.com.gado.controllers;

import br.com.gado.dto.grupoProdutoDto.GrupoProdutoCadastroDto;
import br.com.gado.dto.grupoProdutoDto.GrupoProdutoPutDto;
import br.com.gado.dto.grupoProdutoDto.GrupoProdutoRespostaDto;
import br.com.gado.services.SGrupoProduto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/grupos-produto")
public class CGrupoProduto {

    @Autowired
    private SGrupoProduto grupoProdutoService;

    @GetMapping
    public List<GrupoProdutoRespostaDto> getGrupos(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status) {
        return grupoProdutoService.listar(busca, status);
    }

    @GetMapping("/{id}")
    public GrupoProdutoRespostaDto getGrupoPorId(@PathVariable Long id) {
        return grupoProdutoService.buscarPorId(id);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FINANCEIRO')")
    @PostMapping
    public GrupoProdutoRespostaDto postGrupo(@Valid @RequestBody GrupoProdutoCadastroDto dto) {
        return grupoProdutoService.criar(dto);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FINANCEIRO')")
    @PutMapping("/{id}")
    public GrupoProdutoRespostaDto putGrupo(@PathVariable Long id, @Valid @RequestBody GrupoProdutoPutDto dto) {
        return grupoProdutoService.atualizar(id, dto);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FINANCEIRO')")
    @DeleteMapping("/{id}")
    public String deleteGrupo(@PathVariable Long id) {
        return grupoProdutoService.inativar(id);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FINANCEIRO')")
    @PutMapping("/{id}/reativar")
    public String reativarGrupo(@PathVariable Long id) {
        return grupoProdutoService.reativar(id);
    }
}
