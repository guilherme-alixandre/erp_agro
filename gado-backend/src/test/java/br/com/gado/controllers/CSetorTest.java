package br.com.gado.controllers;

import br.com.gado.dto.SetorDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SPdfRelatorio;
import br.com.gado.services.SSetor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CSetor.class)
class CSetorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SSetor setorService;

    @MockitoBean
    private SPdfRelatorio pdfService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "admin@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void getSetor_deveRetornar200() throws Exception {
        SetorDto dto = new SetorDto();
        dto.setId(1L);
        dto.setNome("Pasto 1");
        when(setorService.procuraPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/setores/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pasto 1"));
    }

    @Test
    void getSetor_deveRetornar500_QuandoNaoEncontrado() throws Exception {
        when(setorService.procuraPorId(99L)).thenThrow(new EntityNotFoundException("nenhum setor encontrado"));

        mockMvc.perform(get("/api/setores/99")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getSetores_deveRetornar200() throws Exception {
        when(setorService.buscarTodos()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/setores")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk());
    }

    @Test
    void postSetor_deveRetornar201() throws Exception {
        SetorDto entrada = new SetorDto();
        entrada.setNome("Pasto Novo");
        SetorDto criado = new SetorDto();
        criado.setId(1L);
        criado.setNome("Pasto Novo");
        when(setorService.cadastra(any(SetorDto.class), eq(USUARIO))).thenReturn(criado);

        mockMvc.perform(post("/api/setores")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteSetor_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/setores/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteSetor_deveRetornar409_QuandoServicoLancaExcecao() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Setor possui lotes alocados"))
                .when(setorService).deleta(1L);

        mockMvc.perform(delete("/api/setores/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(content().string("Setor possui lotes alocados"));
    }
}
