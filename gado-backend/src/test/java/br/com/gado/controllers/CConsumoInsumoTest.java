package br.com.gado.controllers;

import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoCadastroDto;
import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoRespostaDto;
import br.com.gado.dto.consumoInsumoDto.SobraAlimentacaoCadastroDto;
import br.com.gado.dto.consumoInsumoDto.SobraAlimentacaoRespostaDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SConsumoInsumo;
import br.com.gado.services.SSobraAlimentacao;
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

@WebMvcTest(CConsumoInsumo.class)
class CConsumoInsumoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SConsumoInsumo consumoInsumoService;

    @MockitoBean
    private SSobraAlimentacao sobraAlimentacaoService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "cuidador@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void listarPorSetor_deveRetornar200() throws Exception {
        when(consumoInsumoService.listarPorSetor(eq(10L), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/consumos-insumo").param("setorId", "10")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk());
    }

    @Test
    void registrarConsumo_deveRetornar201() throws Exception {
        ConsumoInsumoCadastroDto dto = new ConsumoInsumoCadastroDto();
        dto.setInsumoId(1L);
        dto.setSetorId(10L);
        dto.setQuantidade(5.0);

        ConsumoInsumoRespostaDto criado = new ConsumoInsumoRespostaDto();
        criado.setId(100L);
        when(consumoInsumoService.registrarConsumo(any(ConsumoInsumoCadastroDto.class), eq(USUARIO))).thenReturn(criado);

        mockMvc.perform(post("/api/consumos-insumo")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void registrarConsumo_deveRetornar400_QuandoQuantidadeAusente() throws Exception {
        ConsumoInsumoCadastroDto dto = new ConsumoInsumoCadastroDto();
        dto.setInsumoId(1L);
        dto.setSetorId(10L);

        mockMvc.perform(post("/api/consumos-insumo")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void excluirConsumo_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/consumos-insumo/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void registrarSobra_deveRetornar200() throws Exception {
        SobraAlimentacaoCadastroDto dto = new SobraAlimentacaoCadastroDto();
        dto.setQuantidade(1.0);
        dto.setReaproveitado(true);

        SobraAlimentacaoRespostaDto resposta = new SobraAlimentacaoRespostaDto();
        resposta.setConsumoInsumoId(1L);
        when(sobraAlimentacaoService.registrarOuAtualizarSobra(eq(1L), any(SobraAlimentacaoCadastroDto.class), eq(USUARIO)))
                .thenReturn(resposta);

        mockMvc.perform(put("/api/consumos-insumo/1/sobra")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consumoInsumoId").value(1));
    }

    @Test
    void excluirSobra_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/consumos-insumo/1/sobra")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
