package br.com.gado.controllers;

import br.com.gado.dto.rentabilidadeDto.ContaParceiroDto;
import br.com.gado.dto.rentabilidadeDto.GastoGrupoDto;
import br.com.gado.dto.rentabilidadeDto.ProdutoVendidoDto;
import br.com.gado.dto.rentabilidadeDto.ResumoPeriodoDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SPdfRelatorio;
import br.com.gado.services.SRentabilidade;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CRentabilidade.class)
class CRentabilidadeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SRentabilidade rentabilidadeService;

    @MockitoBean
    private SPdfRelatorio pdfService;

    @MockitoBean
    private JwtService jwtService;

    private static final String USUARIO = "financeiro@fazenda.com";
    private static final String DATA_INICIO = "2026-01-01";
    private static final String DATA_FIM = "2026-12-31";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void gerarResumoPeriodo_deveRetornar200() throws Exception {
        ResumoPeriodoDto dto = new ResumoPeriodoDto(LocalDate.parse(DATA_INICIO), LocalDate.parse(DATA_FIM),
                new BigDecimal("1000.00"), new BigDecimal("200.00"), new BigDecimal("100.00"),
                new BigDecimal("300.00"), new BigDecimal("700.00"), List.of());
        when(rentabilidadeService.gerarResumoPeriodo(eq(LocalDate.parse(DATA_INICIO)), eq(LocalDate.parse(DATA_FIM)), eq(USUARIO)))
                .thenReturn(dto);

        mockMvc.perform(get("/api/financeiro/rentabilidade/resumo")
                        .param("dataInicio", DATA_INICIO)
                        .param("dataFim", DATA_FIM)
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lucroLiquido").value(700.00));
    }

    @Test
    void listarContasPorParceiro_deveRetornar200() throws Exception {
        ContaParceiroDto dto = new ContaParceiroDto(1L, "Fornecedor A", new BigDecimal("500.00"),
                BigDecimal.ZERO, 2, 0, LocalDate.parse("2026-02-10"));
        when(rentabilidadeService.listarContasPorParceiro(any(), any(), eq(USUARIO))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/financeiro/rentabilidade/contas-parceiro")
                        .param("dataInicio", DATA_INICIO)
                        .param("dataFim", DATA_FIM)
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].parceiroNome").value("Fornecedor A"));
    }

    @Test
    void listarVendasPorProduto_deveRetornar200() throws Exception {
        ProdutoVendidoDto dto = new ProdutoVendidoDto("Leite", new BigDecimal("1000"), "litro(s)", new BigDecimal("2000.00"));
        when(rentabilidadeService.listarVendasPorProduto(any(), any(), eq(USUARIO))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/financeiro/rentabilidade/vendas-produto")
                        .param("dataInicio", DATA_INICIO)
                        .param("dataFim", DATA_FIM)
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Leite"));
    }

    @Test
    void listarGastosPorGrupo_deveRetornar200() throws Exception {
        GastoGrupoDto dto = new GastoGrupoDto("Folha de Pagamento", new BigDecimal("3000.00"));
        when(rentabilidadeService.listarGastosPorGrupo(any(), any(), eq(USUARIO))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/financeiro/rentabilidade/gastos-grupo")
                        .param("dataInicio", DATA_INICIO)
                        .param("dataFim", DATA_FIM)
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].grupoNome").value("Folha de Pagamento"));
    }

    @Test
    void gerarPdf_deveRetornar200ComContentTypePdf() throws Exception {
        when(pdfService.gerarRelatorioRentabilidade(any(), any(), eq(USUARIO))).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/financeiro/rentabilidade/pdf")
                        .param("dataInicio", DATA_INICIO)
                        .param("dataFim", DATA_FIM)
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
