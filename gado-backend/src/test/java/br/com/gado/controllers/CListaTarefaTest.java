package br.com.gado.controllers;

import br.com.gado.dto.ListasTarefasDTO;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SListasTarefas;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CListaTarefa.class)
class CListaTarefaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SListasTarefas listaTarefaService;

    /** JwtAuthenticationFilter (Filter/@Component) entra no slice do @WebMvcTest e precisa de um JwtService. */
    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "user@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        br.com.gado.security.TestSecurity.limpar();
    }

    @Test
    void getListaTarefa_deveRetornar200_QuandoEncontrada() throws Exception {
        ListasTarefasDTO dto = new ListasTarefasDTO();
        dto.setId(1L);
        dto.setNomeLista("Tarefas de João");
        when(listaTarefaService.buscarListaDeTarefasPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/listaTarefa/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeLista").value("Tarefas de João"));
    }

    @Test
    void getListaTarefa_deveRetornar500_QuandoNaoEncontrada() throws Exception {
        when(listaTarefaService.buscarListaDeTarefasPorId(99L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/api/listaTarefa/99")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postListaTarefa_deveCriarECretornar200() throws Exception {
        ListasTarefasDTO entrada = new ListasTarefasDTO();
        entrada.setNomeLista("Nova lista");
        ListasTarefasDTO salva = new ListasTarefasDTO();
        salva.setId(2L);
        salva.setNomeLista("Nova lista");
        when(listaTarefaService.criarListaDeTarefas(any(ListasTarefasDTO.class))).thenReturn(salva);

        mockMvc.perform(post("/api/listaTarefa")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void deleteListaTarefa_deveRetornarMensagem() throws Exception {
        when(listaTarefaService.excluirListaDeTarefas(1L)).thenReturn("lista de tarefas excluída com sucesso");

        mockMvc.perform(delete("/api/listaTarefa/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("lista de tarefas excluída com sucesso"));
    }

    @Test
    void putListaTarefa_deveAtualizarERetornar200() throws Exception {
        ListasTarefasDTO entrada = new ListasTarefasDTO();
        entrada.setNomeLista("Nome atualizado");
        ListasTarefasDTO atualizada = new ListasTarefasDTO();
        atualizada.setId(1L);
        atualizada.setNomeLista("Nome atualizado");
        when(listaTarefaService.atualizarListaDeTarefasPorId(any(ListasTarefasDTO.class), eq(1L))).thenReturn(atualizada);

        mockMvc.perform(put("/api/listaTarefa/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeLista").value("Nome atualizado"));
    }
}
