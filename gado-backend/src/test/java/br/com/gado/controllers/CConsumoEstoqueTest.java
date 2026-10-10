package br.com.gado.controllers;

import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueCadastroDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueCancelamentoDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueItemCadastroDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueRespostaDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SConsumoEstoque;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CConsumoEstoque.class)
class CConsumoEstoqueTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SConsumoEstoque consumoEstoqueService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "cuidador@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void listarTodos_deveRetornar200() throws Exception {
        when(consumoEstoqueService.listarTodos(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/consumo-estoque")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk());
    }

    @Test
    void registrarConsumo_deveRetornar201() throws Exception {
        ConsumoEstoqueItemCadastroDto item = new ConsumoEstoqueItemCadastroDto();
        item.setInsumoId(1L);
        item.setQuantidade(5.0);
        ConsumoEstoqueCadastroDto dto = new ConsumoEstoqueCadastroDto();
        dto.setMotivo("Suplementação");
        dto.setItens(List.of(item));

        ConsumoEstoqueRespostaDto criado = new ConsumoEstoqueRespostaDto();
        criado.setId(1L);
        when(consumoEstoqueService.registrarConsumo(any(ConsumoEstoqueCadastroDto.class), eq(USUARIO))).thenReturn(criado);

        mockMvc.perform(post("/api/consumo-estoque")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void registrarConsumo_deveRetornar400_QuandoMotivoEmBranco() throws Exception {
        ConsumoEstoqueCadastroDto dto = new ConsumoEstoqueCadastroDto();
        dto.setItens(List.of());

        mockMvc.perform(post("/api/consumo-estoque")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelarConsumo_deveRetornar200() throws Exception {
        ConsumoEstoqueCancelamentoDto dto = new ConsumoEstoqueCancelamentoDto();
        dto.setMotivoCancelamento("Erro no lançamento");

        ConsumoEstoqueRespostaDto cancelado = new ConsumoEstoqueRespostaDto();
        cancelado.setId(1L);
        cancelado.setCancelado(true);
        when(consumoEstoqueService.cancelarConsumo(eq(1L), any(ConsumoEstoqueCancelamentoDto.class), eq(USUARIO)))
                .thenReturn(cancelado);

        mockMvc.perform(post("/api/consumo-estoque/1/cancelar")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelado").value(true));
    }
}
