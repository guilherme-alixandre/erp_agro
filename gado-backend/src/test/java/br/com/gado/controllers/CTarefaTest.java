package br.com.gado.controllers;

import br.com.gado.dto.tarefaDto.TarefaAtribuirDto;
import br.com.gado.dto.tarefaDto.TarefaEdicaoDto;
import br.com.gado.dto.tarefaDto.TarefaRespostaDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.STarefa;
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

@WebMvcTest(CTarefa.class)
class CTarefaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private STarefa tarefaService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "user@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        br.com.gado.security.TestSecurity.limpar();
    }

    @Test
    void listarMinhasTarefas_deveRetornar200ComLista() throws Exception {
        TarefaRespostaDto tarefa = new TarefaRespostaDto();
        tarefa.setId(1L);
        tarefa.setDescricao("Alimentar o gado");
        when(tarefaService.listarMinhasTarefas(USUARIO)).thenReturn(List.of(tarefa));

        mockMvc.perform(get("/api/tarefas")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Alimentar o gado"));
    }

    @Test
    void listarTarefasAtribuidasPorMim_deveRetornar200() throws Exception {
        when(tarefaService.listarTarefasAtribuidasPorMim(USUARIO)).thenReturn(List.of());

        mockMvc.perform(get("/api/tarefas/atribuidas")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void atribuirTarefa_deveRetornar201_QuandoDadosValidos() throws Exception {
        TarefaAtribuirDto dto = new TarefaAtribuirDto();
        dto.setDescricao("Vacinar lote 3");
        dto.setAtribuidoParaEmail("destinatario@fazenda.com");

        TarefaRespostaDto criada = new TarefaRespostaDto();
        criada.setId(10L);
        criada.setDescricao("Vacinar lote 3");
        when(tarefaService.atribuirTarefa(any(TarefaAtribuirDto.class), eq(USUARIO))).thenReturn(criada);

        mockMvc.perform(post("/api/tarefas")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void atribuirTarefa_deveRetornar400_QuandoDescricaoEmBranco() throws Exception {
        TarefaAtribuirDto dto = new TarefaAtribuirDto();
        dto.setDescricao("");
        dto.setAtribuidoParaEmail("destinatario@fazenda.com");

        mockMvc.perform(post("/api/tarefas")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void editarTarefa_deveRetornar200() throws Exception {
        TarefaEdicaoDto dto = new TarefaEdicaoDto();
        dto.setStatusConclusao(true);

        TarefaRespostaDto atualizada = new TarefaRespostaDto();
        atualizada.setId(1L);
        atualizada.setStatusConclusao(true);
        when(tarefaService.editarTarefa(eq(1L), any(TarefaEdicaoDto.class), eq(USUARIO))).thenReturn(atualizada);

        mockMvc.perform(put("/api/tarefas/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusConclusao").value(true));
    }

    @Test
    void editarTarefa_deveRetornar500_QuandoTarefaNaoEncontrada() throws Exception {
        when(tarefaService.editarTarefa(eq(99L), any(TarefaEdicaoDto.class), eq(USUARIO)))
                .thenThrow(new EntityNotFoundException("Tarefa não encontrada."));

        mockMvc.perform(put("/api/tarefas/99")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new TarefaEdicaoDto())))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void excluirTarefa_deveRetornarMensagem() throws Exception {
        when(tarefaService.excluirTarefa(1L, USUARIO)).thenReturn("Tarefa excluída com sucesso.");

        mockMvc.perform(delete("/api/tarefas/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Tarefa excluída com sucesso."));
    }

    @Test
    void excluirTarefa_deveRetornar400_QuandoSemPermissao() throws Exception {
        when(tarefaService.excluirTarefa(1L, USUARIO))
                .thenThrow(new IllegalArgumentException("Você só pode editar ou excluir tarefas atribuídas a você ou por você."));

        mockMvc.perform(delete("/api/tarefas/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
