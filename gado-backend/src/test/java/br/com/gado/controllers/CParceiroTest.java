package br.com.gado.controllers;

import br.com.gado.dto.parcerioDto.ParceiroCadastroDto;
import br.com.gado.dto.parcerioDto.ParceiroDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnTipoParceiro;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SParceiro;
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

@WebMvcTest(CParceiro.class)
class CParceiroTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SParceiro parceiroService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "gerente@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void getParceiros_deveRetornar200() throws Exception {
        when(parceiroService.listarTodos(null, USUARIO)).thenReturn(List.of());

        mockMvc.perform(get("/api/parceiros")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE)))
                .andExpect(status().isOk());
    }

    @Test
    void getParceiro_deveRetornar500_QuandoNaoEncontrado() throws Exception {
        when(parceiroService.buscaPorCPF_CNPJ("99999999999", USUARIO))
                .thenThrow(new EntityNotFoundException("nenhum parceiro encontrado"));

        mockMvc.perform(get("/api/parceiros/99999999999")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postParceiro_deveRetornar200() throws Exception {
        ParceiroCadastroDto dto = new ParceiroCadastroDto();
        dto.setNome("Fornecedor Teste");
        dto.setCPF_CNPJ("12345678909");
        dto.setTipo(EnTipoParceiro.FORNECEDOR);

        ParceiroDto criado = new ParceiroDto();
        criado.setNome("Fornecedor Teste");
        when(parceiroService.cadastra(any(ParceiroCadastroDto.class), eq(USUARIO))).thenReturn(criado);

        mockMvc.perform(post("/api/parceiros")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Fornecedor Teste"));
    }

    @Test
    void deleteParceiro_deveRetornarMensagem() throws Exception {
        when(parceiroService.deleta("12345678909", USUARIO)).thenReturn("Parceiro deletado com sucesso");

        mockMvc.perform(delete("/api/parceiros/12345678909")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Parceiro deletado com sucesso"));
    }
}
