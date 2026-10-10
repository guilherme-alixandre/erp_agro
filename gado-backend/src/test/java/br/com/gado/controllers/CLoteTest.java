package br.com.gado.controllers;

import br.com.gado.dto.loteDto.CustoRacaoLoteDto;
import br.com.gado.dto.loteDto.LoteCadastroDto;
import br.com.gado.dto.loteDto.LoteDto;
import br.com.gado.dto.loteDto.LoteSetorCadastroDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SLote;
import br.com.gado.services.SPdfRelatorio;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CLote.class)
class CLoteTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SLote loteService;

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
    void getLote_deveRetornar200() throws Exception {
        LoteDto dto = new LoteDto();
        dto.setId(1L);
        dto.setCodigo("LOT001");
        when(loteService.buscaPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/lotes/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("LOT001"));
    }

    @Test
    void getLote_deveRetornar500_QuandoNaoEncontrado() throws Exception {
        when(loteService.buscaPorId(99L)).thenThrow(new EntityNotFoundException("Nenhum lote ativo encontrado para o ID: 99"));

        mockMvc.perform(get("/api/lotes/99")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getCustoRacao_deveRetornar200() throws Exception {
        when(loteService.calcularCustoRacaoAcumulado(1L))
                .thenReturn(new CustoRacaoLoteDto(1L, new BigDecimal("10.00"), new BigDecimal("10.00")));

        mockMvc.perform(get("/api/lotes/1/custo-racao")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custoTotalAcumulado").value(10.00));
    }

    @Test
    void postLote_deveRetornar200_ComMensagem() throws Exception {
        LoteSetorCadastroDto alocacao = new LoteSetorCadastroDto();
        alocacao.setSetorId(1L);
        LoteCadastroDto dto = new LoteCadastroDto();
        dto.setCorBrinco("Azul");
        dto.setAlocacoes(List.of(alocacao));

        when(loteService.cadastra(eq(USUARIO), any(LoteCadastroDto.class)))
                .thenReturn("Lote LOT001 cadastrado com sucesso.");

        mockMvc.perform(post("/api/lotes")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Lote LOT001 cadastrado com sucesso."));
    }

    @Test
    void postLote_deveRetornar400_QuandoCorBrincoAusente() throws Exception {
        LoteCadastroDto dto = new LoteCadastroDto();
        dto.setAlocacoes(List.of());

        mockMvc.perform(post("/api/lotes")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteLote_deveRetornarMensagem() throws Exception {
        when(loteService.deleta(1L, USUARIO)).thenReturn("Lote LOT001 excluído com sucesso.");

        mockMvc.perform(delete("/api/lotes/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Lote LOT001 excluído com sucesso."));
    }
}
