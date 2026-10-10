package br.com.gado.controllers;

import br.com.gado.dto.AnimalDto;
import br.com.gado.dto.custoAnimalDto.CustoVidaAnimalDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SAnimal;
import br.com.gado.services.SCustoAnimal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CAnimal.class)
class CAnimalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SAnimal animalService;

    @MockitoBean
    private SCustoAnimal custoAnimalService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "cuidador@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void getAnimal_deveRetornar200() throws Exception {
        AnimalDto dto = new AnimalDto();
        dto.setCodigoBrinco("NE0001");
        when(animalService.buscarPorBrinco("NE0001")).thenReturn(dto);

        mockMvc.perform(get("/api/animais/NE0001")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoBrinco").value("NE0001"));
    }

    @Test
    void getAnimal_deveRetornar500_QuandoNaoEncontrado() throws Exception {
        when(animalService.buscarPorBrinco("XX9999")).thenThrow(new EntityNotFoundException("animal não encontrado"));

        mockMvc.perform(get("/api/animais/XX9999")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAnimals_deveRetornar200ComLista() throws Exception {
        when(animalService.buscarTodosAnimais(null)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/animais")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk());
    }

    @Test
    void postAnimal_deveRetornar201() throws Exception {
        AnimalDto dto = new AnimalDto();
        dto.setRacaId(1L);
        dto.setPesoAtual(200.0);

        AnimalDto criado = new AnimalDto();
        criado.setCodigoBrinco("NE0001");
        when(animalService.cadastraAnimal(eq(USUARIO), any(AnimalDto.class))).thenReturn(criado);

        mockMvc.perform(post("/api/animais/usuarios/{email}", USUARIO)
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoBrinco").value("NE0001"));
    }

    @Test
    void postAnimal_deveRetornar400_QuandoPesoAbaixoDoMinimo() throws Exception {
        AnimalDto dto = new AnimalDto();
        dto.setRacaId(1L);
        dto.setPesoAtual(5.0);

        mockMvc.perform(post("/api/animais/usuarios/{email}", USUARIO)
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCustoVida_deveRetornar200ComOsTotais() throws Exception {
        CustoVidaAnimalDto dto = new CustoVidaAnimalDto(1L,
                java.math.BigDecimal.valueOf(1000), java.math.BigDecimal.valueOf(50),
                java.math.BigDecimal.valueOf(10), java.math.BigDecimal.valueOf(1060), new ArrayList<>());
        when(custoAnimalService.calcularCustoVida(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/animais/{id}/custo-vida", 1L)
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGeral").value(1060))
                .andExpect(jsonPath("$.totalAquisicao").value(1000));
    }

    @Test
    void deleteAnimal_deveRetornarMensagem() throws Exception {
        when(animalService.deletaAnimal("NE0001")).thenReturn("Animal deletado com sucesso");

        mockMvc.perform(delete("/api/animais/NE0001")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.CUIDADOR))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Animal deletado com sucesso"));
    }
}
