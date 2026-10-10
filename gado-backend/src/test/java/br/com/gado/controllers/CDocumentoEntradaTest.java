package br.com.gado.controllers;

import br.com.gado.dto.documentoEntradaDto.DocumentoEntradaRespostaDto;
import br.com.gado.dto.documentoEntradaDto.RecusaDocumentoDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SDocumentoEntrada;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CDocumentoEntrada.class)
class CDocumentoEntradaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SDocumentoEntrada documentoEntradaService;

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
        when(documentoEntradaService.listarTodos(USUARIO)).thenReturn(List.of());

        mockMvc.perform(get("/api/documentos-entrada")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO)))
                .andExpect(status().isOk());
    }

    @Test
    void importarNfeXml_deveRetornar201() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "nfe.xml", "text/xml", "<nfeProc/>".getBytes());
        DocumentoEntradaRespostaDto criado = new DocumentoEntradaRespostaDto();
        criado.setId(1L);
        when(documentoEntradaService.importarNfeXml(any(), eq(1L), eq(USUARIO))).thenReturn(criado);

        mockMvc.perform(multipart("/api/documentos-entrada/importar-nfe")
                        .file(file)
                        .param("fornecedorId", "1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void aprovarDocumento_deveRetornar200() throws Exception {
        DocumentoEntradaRespostaDto aprovado = new DocumentoEntradaRespostaDto();
        aprovado.setId(1L);
        when(documentoEntradaService.aprovarDocumento(1L, USUARIO)).thenReturn(aprovado);

        mockMvc.perform(post("/api/documentos-entrada/1/aprovar")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void recusarDocumento_deveRetornar400_QuandoJustificativaEmBranco() throws Exception {
        RecusaDocumentoDto dto = new RecusaDocumentoDto();

        mockMvc.perform(post("/api/documentos-entrada/1/recusar")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void excluirDocumento_deveRetornarMensagem() throws Exception {
        when(documentoEntradaService.excluirDocumento(1L, USUARIO)).thenReturn("Documento excluído com sucesso.");

        mockMvc.perform(delete("/api/documentos-entrada/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.FINANCEIRO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Documento excluído com sucesso."));
    }
}
