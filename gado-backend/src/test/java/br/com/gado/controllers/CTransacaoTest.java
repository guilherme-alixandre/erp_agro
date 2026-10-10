package br.com.gado.controllers;

import br.com.gado.dto.TrasacaoDTO;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.STrasacao;
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

@WebMvcTest(CTransacao.class)
class CTransacaoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private STrasacao trasacaoService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "financeiro@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void getTransacao_deveRetornar200() throws Exception {
        TrasacaoDTO dto = new TrasacaoDTO();
        dto.setId(1L);
        dto.setValor(1000.0);
        when(trasacaoService.buscarTrasacaoPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/transacao/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(1000.0));
    }

    @Test
    void getTransacao_deveRetornar500_QuandoNaoEncontrada() throws Exception {
        when(trasacaoService.buscarTrasacaoPorId(99L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/api/transacao/99")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postTransacao_deveRetornar200() throws Exception {
        TrasacaoDTO entrada = new TrasacaoDTO();
        entrada.setValor(1000.0);
        TrasacaoDTO salva = new TrasacaoDTO();
        salva.setId(1L);
        salva.setValor(1000.0);
        when(trasacaoService.criarTrasacao(any(TrasacaoDTO.class))).thenReturn(salva);

        mockMvc.perform(post("/api/transacao")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteTransacao_deveRetornarMensagem() throws Exception {
        when(trasacaoService.excluirTrasacaoPorId(1L)).thenReturn("transação excluída com sucesso");

        mockMvc.perform(delete("/api/transacao/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("transação excluída com sucesso"));
    }
}
