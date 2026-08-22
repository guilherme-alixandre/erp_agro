package br.com.gado.controllers;

import br.com.gado.dto.folhaPagamentoDto.EstornoPagamentoDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioCadastroDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioPutDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioRespostaDto;
import br.com.gado.dto.folhaPagamentoDto.PagamentoFuncionarioCadastroDto;
import br.com.gado.dto.folhaPagamentoDto.PagamentoFuncionarioRespostaDto;
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
    public List<FuncionarioRespostaDto> listarFuncionarios(
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return folhaPagamentoService.listarFuncionarios(emailUsuario);
    }

    @PostMapping("/funcionarios")
    public ResponseEntity<FuncionarioRespostaDto> cadastrarFuncionario(
            @Valid @RequestBody FuncionarioCadastroDto dto,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        FuncionarioRespostaDto criado = folhaPagamentoService.cadastrarFuncionario(dto, emailUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/funcionarios/{id}")
    public FuncionarioRespostaDto atualizarFuncionario(
            @PathVariable Long id,
            @Valid @RequestBody FuncionarioPutDto dto,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return folhaPagamentoService.atualizarFuncionario(id, dto, emailUsuario);
    }

    @GetMapping("/pagamentos")
    public List<PagamentoFuncionarioRespostaDto> listarPagamentosPorBloco(
            @RequestParam int ano,
            @RequestParam int mes,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return folhaPagamentoService.listarPagamentosPorBloco(ano, mes, emailUsuario);
    }

    @PostMapping("/pagamentos")
    public ResponseEntity<PagamentoFuncionarioRespostaDto> lancarPagamento(
            @Valid @RequestBody PagamentoFuncionarioCadastroDto dto,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        PagamentoFuncionarioRespostaDto criado = folhaPagamentoService.lancarPagamento(dto, emailUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PostMapping("/pagamentos/{id}/estornar")
    public PagamentoFuncionarioRespostaDto estornarPagamento(
            @PathVariable Long id,
            @Valid @RequestBody EstornoPagamentoDto dto,
            @RequestHeader(name = "X-Usuario-Email", required = false) String emailUsuario) {
        return folhaPagamentoService.estornarPagamento(id, dto, emailUsuario);
    }
}
