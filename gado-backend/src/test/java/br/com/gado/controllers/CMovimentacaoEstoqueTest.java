package br.com.gado.controllers;

import br.com.gado.dto.MovimentacaoEstoqueDTO;
import br.com.gado.dto.movimentacaoEstoqueDto.MovimentacaoEstoqueRespostaDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SMovimentacaoEstoque;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CMovimentacaoEstoque.class)
class CMovimentacaoEstoqueTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SMovimentacaoEstoque movimentacaoEstoqueService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "admin@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void listar_deveRetornar200() throws Exception {
        MovimentacaoEstoqueRespostaDto dto = new MovimentacaoEstoqueRespostaDto();
        dto.setId(1L);
        when(movimentacaoEstoqueService.listarTodas(null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/movimentacaoEstoque")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void postMovimentacaoEstoque_deveRetornar200() throws Exception {
        MovimentacaoEstoqueDTO dto = new MovimentacaoEstoqueDTO();
        dto.setQuantidade(10.0);

        MovimentacaoEstoqueDTO salva = new MovimentacaoEstoqueDTO();
        salva.setId(1L);
        salva.setQuantidade(10.0);
        when(movimentacaoEstoqueService.criarMovimentacaoEstoque(any(MovimentacaoEstoqueDTO.class))).thenReturn(salva);

        mockMvc.perform(post("/api/movimentacaoEstoque")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteMovimentacaoEstoque_deveRetornarMensagem() throws Exception {
        when(movimentacaoEstoqueService.excluirMovimentacaoEstoquePorId(1L))
                .thenReturn("movimentação de estoque excluída com sucesso");

        mockMvc.perform(delete("/api/movimentacaoEstoque/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("movimentação de estoque excluída com sucesso"));
    }
}
