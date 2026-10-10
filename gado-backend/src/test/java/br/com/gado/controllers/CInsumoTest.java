package br.com.gado.controllers;

import br.com.gado.dto.insumoDto.EntradaEstoqueDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueCadastroDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueRespostaDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnTipoInsumo;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SInsumo;
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

@WebMvcTest(CInsumo.class)
class CInsumoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SInsumo insumoService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "admin@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void getEstoque_deveRetornar200ComLista() throws Exception {
        InsumoEstoqueRespostaDto dto = new InsumoEstoqueRespostaDto();
        dto.setId(1L);
        dto.setNome("Ração");
        when(insumoService.listarEstoque(null, null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/insumos/estoque")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ração"));
    }

    @Test
    void getEstoquePorId_deveRetornar500_QuandoNaoEncontrado() throws Exception {
        when(insumoService.buscarEstoquePorId(99L)).thenThrow(new EntityNotFoundException("Insumo não encontrado ou inativo."));

        mockMvc.perform(get("/api/insumos/estoque/99")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postInsumoEstoque_deveCriarERetornar200() throws Exception {
        InsumoEstoqueCadastroDto dto = new InsumoEstoqueCadastroDto();
        dto.setNome("Ração Bovina");
        dto.setTipo(EnTipoInsumo.RACAO);
        dto.setGrupoProdutoId(1L);
        dto.setUnidadeMedidaPrimariaId(1L);

        InsumoEstoqueRespostaDto criado = new InsumoEstoqueRespostaDto();
        criado.setId(10L);
        criado.setNome("Ração Bovina");
        when(insumoService.criarInsumoEstoque(any(InsumoEstoqueCadastroDto.class), eq(USUARIO))).thenReturn(criado);

        mockMvc.perform(post("/api/insumos/estoque")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void postInsumoEstoque_deveRetornar400_QuandoNomeEmBranco() throws Exception {
        InsumoEstoqueCadastroDto dto = new InsumoEstoqueCadastroDto();
        dto.setTipo(EnTipoInsumo.RACAO);
        dto.setGrupoProdutoId(1L);
        dto.setUnidadeMedidaPrimariaId(1L);

        mockMvc.perform(post("/api/insumos/estoque")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postInsumoEstoque_deveRetornar400_QuandoServicoRejeitaPermissao() throws Exception {
        InsumoEstoqueCadastroDto dto = new InsumoEstoqueCadastroDto();
        dto.setNome("Ração Bovina");
        dto.setTipo(EnTipoInsumo.RACAO);
        dto.setGrupoProdutoId(1L);
        dto.setUnidadeMedidaPrimariaId(1L);
        when(insumoService.criarInsumoEstoque(any(InsumoEstoqueCadastroDto.class), eq(USUARIO)))
                .thenThrow(new IllegalArgumentException("Apenas Administradores, Gerentes ou Cuidadores Chefe podem gerenciar o estoque de insumos."));

        mockMvc.perform(post("/api/insumos/estoque")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postEntradaEstoque_deveRetornar200() throws Exception {
        EntradaEstoqueDto dto = new EntradaEstoqueDto();
        dto.setQuantidade(10.0);
        dto.setPrecoUnitario(5.0);

        InsumoEstoqueRespostaDto resposta = new InsumoEstoqueRespostaDto();
        resposta.setId(1L);
        resposta.setSaldoAtual(10.0);
        when(insumoService.registrarEntradaEstoque(eq(1L), any(EntradaEstoqueDto.class), eq(USUARIO))).thenReturn(resposta);

        mockMvc.perform(post("/api/insumos/estoque/1/entradas")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoAtual").value(10.0));
    }

    @Test
    void deleteInsumoEstoque_deveRetornarMensagem() throws Exception {
        when(insumoService.inativarInsumo(1L, USUARIO)).thenReturn("Produto inativado com sucesso.");

        mockMvc.perform(delete("/api/insumos/estoque/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Produto inativado com sucesso."));
    }
}
