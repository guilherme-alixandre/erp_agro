package br.com.gado.controllers;

import br.com.gado.dto.documentoSaidaDto.DocumentoSaidaRespostaDto;
import br.com.gado.dto.documentoSaidaDto.VendaAnimalCadastroDto;
import br.com.gado.dto.documentoSaidaDto.VendaLeiteCadastroDto;
import br.com.gado.dto.documentoSaidaDto.VendaLeiteItemCadastroDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatusAnimal;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SDocumentoSaida;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CDocumentoSaida.class)
class CDocumentoSaidaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SDocumentoSaida documentoSaidaService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "financeiro@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void listarTodos_deveRetornar200() throws Exception {
        when(documentoSaidaService.listarTodos(USUARIO)).thenReturn(List.of());

        mockMvc.perform(get("/api/documentos-saida")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isOk());
    }

    @Test
    void cadastrarVendaLeite_deveRetornar201() throws Exception {
        VendaLeiteItemCadastroDto item = new VendaLeiteItemCadastroDto();
        item.setLoteId(1L);
        item.setLitros(new BigDecimal("100"));
        VendaLeiteCadastroDto dto = new VendaLeiteCadastroDto();
        dto.setDataEmissao(LocalDate.of(2026, 3, 1));
        dto.setCompradorId(1L);
        dto.setPrecoLitro(new BigDecimal("2.50"));
        dto.setItens(List.of(item));

        DocumentoSaidaRespostaDto criado = new DocumentoSaidaRespostaDto();
        criado.setId(1L);
        when(documentoSaidaService.cadastrarVendaLeite(any(VendaLeiteCadastroDto.class), eq(USUARIO))).thenReturn(criado);

        mockMvc.perform(post("/api/documentos-saida/venda-leite")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void cadastrarVendaAnimal_deveRetornar400_QuandoDestinoAusente() throws Exception {
        VendaAnimalCadastroDto dto = new VendaAnimalCadastroDto();
        dto.setDataEmissao(LocalDate.of(2026, 3, 1));
        dto.setCompradorId(1L);
        dto.setAnimalIds(List.of(1L));
        dto.setValorTotal(new BigDecimal("1000"));

        mockMvc.perform(post("/api/documentos-saida/venda-animal")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastrarVendaAnimal_deveRetornar201() throws Exception {
        VendaAnimalCadastroDto dto = new VendaAnimalCadastroDto();
        dto.setDataEmissao(LocalDate.of(2026, 3, 1));
        dto.setCompradorId(1L);
        dto.setAnimalIds(List.of(1L));
        dto.setValorTotal(new BigDecimal("1000"));
        dto.setDestino(EnStatusAnimal.VENDIDO);

        DocumentoSaidaRespostaDto criado = new DocumentoSaidaRespostaDto();
        criado.setId(2L);
        when(documentoSaidaService.cadastrarVendaAnimal(any(VendaAnimalCadastroDto.class), eq(USUARIO))).thenReturn(criado);

        mockMvc.perform(post("/api/documentos-saida/venda-animal")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }
}
