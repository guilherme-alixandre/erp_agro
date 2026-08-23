package br.com.gado.controllers;

import br.com.gado.dto.folhaPagamentoDto.EstornoPagamentoDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioCadastroDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioPutDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioRespostaDto;
import br.com.gado.dto.folhaPagamentoDto.PagamentoFuncionarioCadastroDto;
import br.com.gado.dto.folhaPagamentoDto.PagamentoFuncionarioRespostaDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.SFolhaPagamento;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/folha-pagamento")
public class CFolhaPagamento {

    @Autowired
    private SFolhaPagamento folhaPagamentoService;

    @GetMapping("/funcionarios")
    public List<FuncionarioRespostaDto> listarFuncionarios() {
        return folhaPagamentoService.listarFuncionarios(SecurityUtils.currentUserEmail());
    }

    @PostMapping("/funcionarios")
    public ResponseEntity<FuncionarioRespostaDto> cadastrarFuncionario(@Valid @RequestBody FuncionarioCadastroDto dto) {
        FuncionarioRespostaDto criado = folhaPagamentoService.cadastrarFuncionario(dto, SecurityUtils.currentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/funcionarios/{id}")
    public FuncionarioRespostaDto atualizarFuncionario(@PathVariable Long id, @Valid @RequestBody FuncionarioPutDto dto) {
        return folhaPagamentoService.atualizarFuncionario(id, dto, SecurityUtils.currentUserEmail());
    }

    @GetMapping("/pagamentos")
    public List<PagamentoFuncionarioRespostaDto> listarPagamentosPorBloco(
            @RequestParam int ano,
            @RequestParam int mes) {
        return folhaPagamentoService.listarPagamentosPorBloco(ano, mes, SecurityUtils.currentUserEmail());
    }

    @PostMapping("/pagamentos")
    public ResponseEntity<PagamentoFuncionarioRespostaDto> lancarPagamento(@Valid @RequestBody PagamentoFuncionarioCadastroDto dto) {
        PagamentoFuncionarioRespostaDto criado = folhaPagamentoService.lancarPagamento(dto, SecurityUtils.currentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PostMapping("/pagamentos/{id}/estornar")
    public PagamentoFuncionarioRespostaDto estornarPagamento(@PathVariable Long id, @Valid @RequestBody EstornoPagamentoDto dto) {
        return folhaPagamentoService.estornarPagamento(id, dto, SecurityUtils.currentUserEmail());
    }
}
