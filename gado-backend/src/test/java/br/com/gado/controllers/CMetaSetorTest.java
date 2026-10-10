package br.com.gado.controllers;

import br.com.gado.dto.metaSetorDto.MedicaoMetaCadastroDto;
import br.com.gado.dto.metaSetorDto.MetaSetorCadastroDto;
import br.com.gado.dto.metaSetorDto.MetaSetorRespostaDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnTipoMeta;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SMetaSetor;
import br.com.gado.services.SPdfRelatorio;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CMetaSetor.class)
class CMetaSetorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SMetaSetor metaSetorService;

    @MockitoBean
    private SPdfRelatorio pdfService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "gerente@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void listarPorSetor_deveRetornar200() throws Exception {
        when(metaSetorService.listarPorSetor(1L)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/metas-setor").param("setorId", "1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE)))
                .andExpect(status().isOk());
    }

    @Test
    void buscarPorId_deveRetornar200() throws Exception {
        MetaSetorRespostaDto dto = new MetaSetorRespostaDto();
        dto.setId(1L);
        dto.setSetorNome("Pasto 1");
        when(metaSetorService.buscarPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/metas-setor/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setorNome").value("Pasto 1"));
    }

    @Test
    void cadastrar_deveValidarPermissaoEDelegar() throws Exception {
        MetaSetorCadastroDto dto = new MetaSetorCadastroDto();
        dto.setSetorId(1L);
        dto.setDataInicial(LocalDate.of(2026, 1, 1));
        dto.setDataFinal(LocalDate.of(2026, 1, 31));
        dto.setTipoMeta(EnTipoMeta.LEITE);
        dto.setQuantidadeEsperada(1000.0);
        dto.setPrecoMedio(2.5);
        when(metaSetorService.cadastrar(any(MetaSetorCadastroDto.class))).thenReturn("Meta do setor cadastrada com sucesso.");

        mockMvc.perform(post("/api/metas-setor")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Meta do setor cadastrada com sucesso."));

        verify(metaSetorService).validaAdminOuGerente(USUARIO);
    }

    @Test
    void cadastrar_deveRetornar400_QuandoQuantidadeEsperadaAusente() throws Exception {
        MetaSetorCadastroDto dto = new MetaSetorCadastroDto();
        dto.setSetorId(1L);
        dto.setDataInicial(LocalDate.of(2026, 1, 1));
        dto.setDataFinal(LocalDate.of(2026, 1, 31));
        dto.setTipoMeta(EnTipoMeta.LEITE);
        dto.setPrecoMedio(2.5);

        mockMvc.perform(post("/api/metas-setor")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastrarMedicao_deveValidarUsuarioEDelegar() throws Exception {
        MedicaoMetaCadastroDto dto = new MedicaoMetaCadastroDto();
        dto.setMetaSetorId(1L);
        dto.setLoteId(1L);
        dto.setDataMedicao(LocalDate.now());
        dto.setQuantidadeLancada(100.0);
        when(metaSetorService.cadastrarMedicao(any(MedicaoMetaCadastroDto.class), eq(USUARIO)))
                .thenReturn("Medição cadastrada com sucesso.");

        mockMvc.perform(post("/api/metas-setor/medicoes")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Medição cadastrada com sucesso."));
    }

    @Test
    void deletarMedicao_deveRetornarMensagem() throws Exception {
        when(metaSetorService.validarEDeletarMedicao(1L, USUARIO)).thenReturn("Medição removida com sucesso.");

        mockMvc.perform(delete("/api/metas-setor/medicoes/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Medição removida com sucesso."));
    }
}
