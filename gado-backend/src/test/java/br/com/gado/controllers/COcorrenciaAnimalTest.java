package br.com.gado.controllers;

import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalCadastroDto;
import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalRespostaDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnTipoOcorrencia;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SOcorrenciaAnimal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(COcorrenciaAnimal.class)
class COcorrenciaAnimalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SOcorrenciaAnimal ocorrenciaAnimalService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "cuidador@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void listarPorAnimal_deveRetornar200() throws Exception {
        when(ocorrenciaAnimalService.listarPorAnimal(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/ocorrenciaAnimal").param("animalId", "1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk());
    }

    @Test
    void cadastrar_deveRetornar201() throws Exception {
        OcorrenciaAnimalCadastroDto dto = new OcorrenciaAnimalCadastroDto();
        dto.setAnimalId(1L);
        dto.setTipoOcorrencia(EnTipoOcorrencia.PESAGEM);
        dto.setDataOcorrencia(new Date());

        OcorrenciaAnimalRespostaDto criada = new OcorrenciaAnimalRespostaDto();
        criada.setId(1L);
        when(ocorrenciaAnimalService.criarOcorrenciaPorAnimalId(any(OcorrenciaAnimalCadastroDto.class)))
                .thenReturn(criada);

        mockMvc.perform(post("/api/ocorrenciaAnimal")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void cadastrar_deveRetornar400_QuandoTipoAusente() throws Exception {
        OcorrenciaAnimalCadastroDto dto = new OcorrenciaAnimalCadastroDto();
        dto.setAnimalId(1L);
        dto.setDataOcorrencia(new Date());

        mockMvc.perform(post("/api/ocorrenciaAnimal")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void excluir_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/ocorrenciaAnimal/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
