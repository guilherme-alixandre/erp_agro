package br.com.gado.controllers;

import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalCadastroDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalCancelamentoDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalRespostaDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SVacinacaoAnimal;
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

@WebMvcTest(CVacinacaoAnimal.class)
class CVacinacaoAnimalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SVacinacaoAnimal vacinacaoAnimalService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "cuidador@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void listar_semAnimalId_deveChamarListarTodos() throws Exception {
        when(vacinacaoAnimalService.listarTodos(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/vacinacoes-animal")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk());
    }

    @Test
    void listar_comAnimalId_deveChamarListarPorAnimal() throws Exception {
        when(vacinacaoAnimalService.listarPorAnimal(5L)).thenReturn(List.of());

        mockMvc.perform(get("/api/vacinacoes-animal").param("animalId", "5")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk());
    }

    @Test
    void registrarAplicacao_deveRetornar201() throws Exception {
        VacinacaoAnimalCadastroDto dto = new VacinacaoAnimalCadastroDto();
        dto.setInsumoId(1L);
        dto.setQuantidadePorAnimal(5.0);
        dto.setAnimalIds(List.of(1L));

        VacinacaoAnimalRespostaDto criado = new VacinacaoAnimalRespostaDto();
        criado.setId(1L);
        when(vacinacaoAnimalService.registrarAplicacao(any(VacinacaoAnimalCadastroDto.class), eq(USUARIO)))
                .thenReturn(criado);

        mockMvc.perform(post("/api/vacinacoes-animal")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void registrarAplicacao_deveRetornar400_QuandoSemAnimais() throws Exception {
        VacinacaoAnimalCadastroDto dto = new VacinacaoAnimalCadastroDto();
        dto.setInsumoId(1L);
        dto.setQuantidadePorAnimal(5.0);
        dto.setAnimalIds(List.of());

        mockMvc.perform(post("/api/vacinacoes-animal")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelarAplicacao_deveRetornar200() throws Exception {
        VacinacaoAnimalCancelamentoDto dto = new VacinacaoAnimalCancelamentoDto();
        dto.setMotivoCancelamento("Erro no lançamento");

        VacinacaoAnimalRespostaDto cancelado = new VacinacaoAnimalRespostaDto();
        cancelado.setId(1L);
        cancelado.setCancelado(true);
        when(vacinacaoAnimalService.cancelarAplicacao(eq(1L), any(VacinacaoAnimalCancelamentoDto.class), eq(USUARIO)))
                .thenReturn(cancelado);

        mockMvc.perform(post("/api/vacinacoes-animal/1/cancelar")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelado").value(true));
    }
}
