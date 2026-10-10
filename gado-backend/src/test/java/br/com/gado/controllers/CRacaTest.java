package br.com.gado.controllers;

import br.com.gado.dto.racaDto.RacaCadastroDto;
import br.com.gado.dto.racaDto.RacaRespostaDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SRaca;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CRaca.class)
class CRacaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SRaca racaService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "admin@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void getRacas_deveRetornar200() throws Exception {
        RacaRespostaDto dto = new RacaRespostaDto();
        dto.setId(1L);
        dto.setNome("Nelore");
        when(racaService.listar(null, null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/racas")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Nelore"));
    }

    @Test
    void getRacaPorId_deveRetornar500_QuandoNaoEncontrada() throws Exception {
        when(racaService.buscarPorId(99L)).thenThrow(new EntityNotFoundException("Raça não encontrada."));

        mockMvc.perform(get("/api/racas/99")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postRaca_deveRetornar200() throws Exception {
        RacaCadastroDto dto = new RacaCadastroDto();
        dto.setNome("Angus");
        dto.setSigla("AN");

        RacaRespostaDto criada = new RacaRespostaDto();
        criada.setId(2L);
        criada.setNome("Angus");
        when(racaService.criar(any(RacaCadastroDto.class), eq(USUARIO))).thenReturn(criada);

        mockMvc.perform(post("/api/racas")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void postRaca_deveRetornar400_QuandoSiglaInvalida() throws Exception {
        RacaCadastroDto dto = new RacaCadastroDto();
        dto.setNome("Angus");
        dto.setSigla("123");

        mockMvc.perform(post("/api/racas")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteRaca_deveRetornarMensagem() throws Exception {
        when(racaService.inativar(1L, USUARIO)).thenReturn("Raça inativada com sucesso.");

        mockMvc.perform(delete("/api/racas/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Raça inativada com sucesso."));
    }
}
