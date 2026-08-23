package br.com.gado.controllers;

import br.com.gado.dto.lancamentoFinanceiroDto.LancamentoFinanceiroRespostaDto;
import br.com.gado.dto.lancamentoFinanceiroDto.LancamentoManualCadastroDto;
import br.com.gado.dto.lancamentoFinanceiroDto.ResumoMensalDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.SLancamentoFinanceiro;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Livro-razão e DRE mensal do módulo financeiro — consolida o que os outros módulos lançam. */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/financeiro/lancamentos")
public class CLancamentoFinanceiro {

    @Autowired
    private SLancamentoFinanceiro lancamentoFinanceiroService;

    @GetMapping
    public List<LancamentoFinanceiroRespostaDto> listarPorBloco(@RequestParam int ano, @RequestParam int mes) {
        return lancamentoFinanceiroService.listarPorBloco(ano, mes, SecurityUtils.currentUserEmail());
    }

    @GetMapping("/resumo-mensal")
    public ResumoMensalDto gerarResumoMensal(@RequestParam int ano, @RequestParam int mes) {
        return lancamentoFinanceiroService.gerarResumoMensal(ano, mes, SecurityUtils.currentUserEmail());
    }

    @PostMapping
    public ResponseEntity<LancamentoFinanceiroRespostaDto> registrarLancamentoManual(@Valid @RequestBody LancamentoManualCadastroDto dto) {
        LancamentoFinanceiroRespostaDto criado = lancamentoFinanceiroService.registrarLancamentoManual(
                dto.getTipoMovimento(), dto.getNaturezaFinanceira(), dto.getDescricao(), dto.getValor(),
                dto.getDataCompetencia(), SecurityUtils.currentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }
}
