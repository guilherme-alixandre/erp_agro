package br.com.gado.controllers;

import br.com.gado.dto.notaFiscalDto.NotaFiscalResumoDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SNotaFiscal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CNotaFiscal.class)
class CNotaFiscalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SNotaFiscal notaFiscalService;

    @MockitoBean
    private JwtService jwtService;

    private static final String USUARIO = "financeiro@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void listar_deveRetornar200ComLista() throws Exception {
        NotaFiscalResumoDto dto = new NotaFiscalResumoDto(
                1L, "ENTRADA", "NF_E", "100", "chave", null, new BigDecimal("100.00"), "APROVADO",
                "Fornecedor Teste", "A_VISTA", null, 1, null);
        when(notaFiscalService.listar(null, null, null, USUARIO)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/notas-fiscais")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroDocumento").value("100"));
    }

    @Test
    void listar_deveRepassarFiltros() throws Exception {
        when(notaFiscalService.listar("100", "chave123", "ENTRADA", USUARIO)).thenReturn(List.of());

        mockMvc.perform(get("/api/notas-fiscais")
                        .param("numero", "100")
                        .param("chave", "chave123")
                        .param("direcao", "ENTRADA")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isOk());
    }
}
