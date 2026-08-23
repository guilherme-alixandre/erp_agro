package br.com.gado.controllers;

import br.com.gado.dto.parcerioDto.ParceiroCadastroDto;
import br.com.gado.dto.parcerioDto.ParceiroDto;
import br.com.gado.dto.parcerioDto.ParceiroPutDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.SParceiro;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/parceiros")
public class CParceiro {

    private final SParceiro parceiroService;

    public CParceiro(SParceiro parceiroService) {
        this.parceiroService = parceiroService;
    }

    @GetMapping
    public List<ParceiroDto> getParceiros(@RequestParam(required = false) String tipo) {
        return parceiroService.listarTodos(tipo, SecurityUtils.currentUserEmail());
    }

    @GetMapping("/{cpf_cnpj}")
    public ParceiroDto getParceiro(@PathVariable String cpf_cnpj) {
        return parceiroService.buscaPorCPF_CNPJ(cpf_cnpj, SecurityUtils.currentUserEmail());
    }

    @PostMapping
    public ParceiroDto postParceiro(@RequestBody ParceiroCadastroDto dto) {
        return parceiroService.cadastra(dto, SecurityUtils.currentUserEmail());
    }

    @DeleteMapping("/{cpf_cnpj}")
    public String deleteParceiro(@PathVariable String cpf_cnpj) {
        return parceiroService.deleta(cpf_cnpj, SecurityUtils.currentUserEmail());
    }

    @PutMapping("/{cpf_cnpj}")
    public ParceiroDto putParceiro(@PathVariable String cpf_cnpj, @RequestBody ParceiroPutDto dto) {
        return parceiroService.altera(cpf_cnpj, dto, SecurityUtils.currentUserEmail());
    }
}
