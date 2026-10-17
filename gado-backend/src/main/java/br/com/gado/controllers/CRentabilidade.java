package br.com.gado.controllers;

import br.com.gado.dto.rentabilidadeDto.ContaParceiroDto;
import br.com.gado.dto.rentabilidadeDto.GastoGrupoDto;
import br.com.gado.dto.rentabilidadeDto.ProdutoVendidoDto;
import br.com.gado.dto.rentabilidadeDto.ResumoPeriodoDto;
import br.com.gado.security.SecurityUtils;
import br.com.gado.services.SPdfRelatorio;
import br.com.gado.services.SRentabilidade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Aba "Rentabilidade" do Financeiro: DRE de período livre, contas a pagar/receber por parceiro,
 * vendas por produto (raça/leite) e maiores gastos por grupo — todos filtrados por dataInicio/dataFim.
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/financeiro/rentabilidade")
public class CRentabilidade {

    @Autowired
    private SRentabilidade rentabilidadeService;

    @Autowired
    private SPdfRelatorio pdfService;

    @GetMapping("/resumo")
    public ResumoPeriodoDto gerarResumoPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return rentabilidadeService.gerarResumoPeriodo(dataInicio, dataFim, SecurityUtils.currentUserEmail());
    }

    @GetMapping("/contas-parceiro")
    public List<ContaParceiroDto> listarContasPorParceiro(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return rentabilidadeService.listarContasPorParceiro(dataInicio, dataFim, SecurityUtils.currentUserEmail());
    }

    @GetMapping("/vendas-produto")
    public List<ProdutoVendidoDto> listarVendasPorProduto(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return rentabilidadeService.listarVendasPorProduto(dataInicio, dataFim, SecurityUtils.currentUserEmail());
    }

    @GetMapping("/gastos-grupo")
    public List<GastoGrupoDto> listarGastosPorGrupo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return rentabilidadeService.listarGastosPorGrupo(dataInicio, dataFim, SecurityUtils.currentUserEmail());
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> gerarPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        byte[] pdf = pdfService.gerarRelatorioRentabilidade(dataInicio, dataFim, SecurityUtils.currentUserEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"relatorio-rentabilidade.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
