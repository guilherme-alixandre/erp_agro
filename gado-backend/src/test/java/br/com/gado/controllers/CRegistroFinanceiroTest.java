package br.com.gado.controllers;

import br.com.gado.dto.RegistroFinanceiroDTO;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SRegistroFinanceiro;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Este controller tem {@code @PreAuthorize} no nível da classe, mas {@code @EnableMethodSecurity}
 * (SecurityConfig) não é carregado neste slice — cobrimos apenas o mapeamento HTTP/serialização.
 */
@WebMvcTest(CRegistroFinanceiro.class)
class CRegistroFinanceiroTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SRegistroFinanceiro registroFinanceiroService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "financeiro@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void getRegistroFinanceiro_deveRetornar200() throws Exception {
        RegistroFinanceiroDTO dto = new RegistroFinanceiroDTO();
        dto.setId(1L);
        dto.setDescricao("Conserto de trator");
        when(registroFinanceiroService.buscarRegistroFinanceiroPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/registroFinanceiro/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Conserto de trator"));
    }

    @Test
    void getRegistroFinanceiro_deveRetornar500_QuandoNaoEncontrado() throws Exception {
        when(registroFinanceiroService.buscarRegistroFinanceiroPorId(99L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/api/registroFinanceiro/99")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postRegistroFinanceiro_deveRetornar200() throws Exception {
        RegistroFinanceiroDTO entrada = new RegistroFinanceiroDTO();
        entrada.setDescricao("Conserto de trator");
        RegistroFinanceiroDTO salvo = new RegistroFinanceiroDTO();
        salvo.setId(1L);
        salvo.setDescricao("Conserto de trator");
        when(registroFinanceiroService.criarRegistroFinanceiro(any(RegistroFinanceiroDTO.class))).thenReturn(salvo);

        mockMvc.perform(post("/api/registroFinanceiro")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteRegistroFinanceiro_deveRetornarMensagem() throws Exception {
        when(registroFinanceiroService.excluirRegistroFinanceiroPorId(1L))
                .thenReturn("registro financeiro excluído com sucesso");

        mockMvc.perform(delete("/api/registroFinanceiro/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("registro financeiro excluído com sucesso"));
    }
}
