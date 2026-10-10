package br.com.gado.controllers;

import br.com.gado.dto.usuarioDto.UsuarioDto;
import br.com.gado.dto.usuarioDto.UsuarioLoginDto;
import br.com.gado.dto.usuarioDto.UsuarioResumoDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SUsuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Este controller usa {@code @PreAuthorize} na maioria dos endpoints, mas
 * {@code @EnableMethodSecurity} não é carregado neste slice — cobrimos apenas o mapeamento
 * HTTP/serialização (ver nota em feedback_test_conventions_gado_backend.md).
 */
@WebMvcTest(CUsuario.class)
class CUsuarioTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SUsuario usuarioService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "admin@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void getUsuariosResumo_deveRetornar200() throws Exception {
        when(usuarioService.buscarResumoTodosAtivos()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/usuarios/resumo")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk());
    }

    @Test
    void getUsuario_deveRetornar200() throws Exception {
        UsuarioDto dto = new UsuarioDto();
        dto.setEmail("cuidador@fazenda.com");
        dto.setNome("Cuidador");
        when(usuarioService.encontraPorEmail("cuidador@fazenda.com")).thenReturn(dto);

        mockMvc.perform(get("/api/usuarios/cuidador@fazenda.com")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cuidador"));
    }

    @Test
    void login_deveRetornar200ComTokenEUsuario() throws Exception {
        UsuarioDto usuarioLogado = new UsuarioDto();
        usuarioLogado.setEmail("cuidador@fazenda.com");
        usuarioLogado.setPerfil(EnPerfilUsuario.CUIDADOR);
        when(usuarioService.login(org.mockito.ArgumentMatchers.any(UsuarioLoginDto.class))).thenReturn(usuarioLogado);
        when(jwtService.gerarToken("cuidador@fazenda.com", EnPerfilUsuario.CUIDADOR)).thenReturn("jwt-gerado");

        UsuarioLoginDto loginDto = new UsuarioLoginDto();
        loginDto.setEmail("cuidador@fazenda.com");
        loginDto.setSenha("senha123");

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-gerado"))
                .andExpect(jsonPath("$.usuario.email").value("cuidador@fazenda.com"));
    }

    @Test
    void login_deveRetornar400_QuandoCredenciaisInvalidas() throws Exception {
        when(usuarioService.login(org.mockito.ArgumentMatchers.any(UsuarioLoginDto.class)))
                .thenThrow(new IllegalArgumentException("Credenciais inválidas."));

        UsuarioLoginDto loginDto = new UsuarioLoginDto();
        loginDto.setEmail("cuidador@fazenda.com");
        loginDto.setSenha("errada");

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUsuario_deveRetornarMensagem() throws Exception {
        when(usuarioService.deleta("cuidador@fazenda.com")).thenReturn("Usuário inativado com sucesso");

        mockMvc.perform(delete("/api/usuarios/cuidador@fazenda.com")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuário inativado com sucesso"));
    }
}
