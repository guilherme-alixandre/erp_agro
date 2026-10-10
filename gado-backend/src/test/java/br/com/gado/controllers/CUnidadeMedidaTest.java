package br.com.gado.controllers;

import br.com.gado.dto.unidadeMedidaDto.UnidadeMedidaCadastroDto;
import br.com.gado.dto.unidadeMedidaDto.UnidadeMedidaRespostaDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SUnidadeMedida;
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

@WebMvcTest(CUnidadeMedida.class)
class CUnidadeMedidaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SUnidadeMedida unidadeMedidaService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "admin@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void getUnidades_deveRetornar200() throws Exception {
        when(unidadeMedidaService.listar(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/unidades-medida")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk());
    }

    @Test
    void postUnidade_deveRetornar200() throws Exception {
        UnidadeMedidaCadastroDto dto = new UnidadeMedidaCadastroDto();
        dto.setUnidade("KG");

        UnidadeMedidaRespostaDto criada = new UnidadeMedidaRespostaDto();
        criada.setId(1L);
        criada.setUnidade("KG");
        when(unidadeMedidaService.criar(any(UnidadeMedidaCadastroDto.class))).thenReturn(criada);

        mockMvc.perform(post("/api/unidades-medida")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade").value("KG"));
    }

    @Test
    void postUnidade_deveRetornar400_QuandoUnidadeEmBranco() throws Exception {
        UnidadeMedidaCadastroDto dto = new UnidadeMedidaCadastroDto();

        mockMvc.perform(post("/api/unidades-medida")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUnidade_deveRetornarMensagem() throws Exception {
        when(unidadeMedidaService.inativar(1L)).thenReturn("Unidade de medida inativada com sucesso");

        mockMvc.perform(delete("/api/unidades-medida/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Unidade de medida inativada com sucesso"));
    }
}
