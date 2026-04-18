package br.com.gado.controllers;

import br.com.gado.application.services.SParceiro;
import br.com.gado.application.dto.parcerioDto.ParceiroCadastroDto;
import br.com.gado.application.dto.parcerioDto.ParceiroPutDto;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/parceiros")
public class CParceiro {

    private final SParceiro parceiroService;

    public CParceiro(SParceiro parceiroService){
        this.parceiroService = parceiroService;
    }


    @GetMapping("/{cpf_cnpj}")
    public Map<String, Object> getParceiro(@PathVariable String cpf_cnpj){
        return parceiroService.buscaPorCPF_CNPJ(cpf_cnpj);
    }

    @PostMapping("/")
    public String postParceiro(@RequestBody ParceiroCadastroDto dto){
        return parceiroService.cadastra(dto);
    }

    @DeleteMapping("/{cpf_cnpj}")
    public String deleteParceiro(@PathVariable String cpf_cnpj){
        return parceiroService.deleta(cpf_cnpj);
    }

    @PutMapping("/{cpf_cnpj}")
    public String putParceiro(@PathVariable String cpf_cnpj,
                              @RequestBody ParceiroPutDto dto)
    {
        return parceiroService.altera(cpf_cnpj, dto);
    }
}
