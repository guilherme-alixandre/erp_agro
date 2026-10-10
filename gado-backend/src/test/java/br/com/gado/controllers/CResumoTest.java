package br.com.gado.controllers;

import br.com.gado.dto.resumoDto.ResumoDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SResumo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CResumo.class)
class CResumoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SResumo resumoService;

    @MockitoBean
    private JwtService jwtService;

    private static final String USUARIO = "cuidador@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void getResumo_deveRetornar200() throws Exception {
        ResumoDto dto = new ResumoDto();
        dto.setFinanceiroVisivel(false);
        when(resumoService.gerarResumo(USUARIO)).thenReturn(dto);

        mockMvc.perform(get("/api/resumo")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financeiroVisivel").value(false));
    }
}
