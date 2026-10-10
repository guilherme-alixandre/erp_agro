package br.com.gado.controllers;

import br.com.gado.dto.CategoriaDTO;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SCategoria;
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

@WebMvcTest(CCategoria.class)
class CCategoriaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SCategoria categoriaService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "admin@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void getCategoria_deveRetornar200() throws Exception {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(1L);
        dto.setCategoria("Manutenção");
        when(categoriaService.buscarCategoriaPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/categoria/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("Manutenção"));
    }

    @Test
    void getCategoria_deveRetornar500_QuandoNaoEncontrada() throws Exception {
        when(categoriaService.buscarCategoriaPorId(99L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/api/categoria/99")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postCategoria_deveRetornar200() throws Exception {
        CategoriaDTO entrada = new CategoriaDTO();
        entrada.setCategoria("Manutenção");
        CategoriaDTO criada = new CategoriaDTO();
        criada.setId(1L);
        criada.setCategoria("Manutenção");
        when(categoriaService.criarCategoria(any(CategoriaDTO.class))).thenReturn(criada);

        mockMvc.perform(post("/api/categoria")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteCategoria_deveRetornarMensagem() throws Exception {
        when(categoriaService.excluirCategoria(1L)).thenReturn("Categoria excluida com sucesso");

        mockMvc.perform(delete("/api/categoria/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Categoria excluida com sucesso"));
    }
}
