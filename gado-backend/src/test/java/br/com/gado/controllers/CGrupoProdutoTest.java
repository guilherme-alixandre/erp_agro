package br.com.gado.controllers;

import br.com.gado.dto.grupoProdutoDto.GrupoProdutoCadastroDto;
import br.com.gado.dto.grupoProdutoDto.GrupoProdutoRespostaDto;
import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnTipoInsumo;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SGrupoProduto;
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

/**
 * O controller usa {@code @PreAuthorize}, mas {@code @EnableMethodSecurity} vem do SecurityConfig
 * real, não carregado neste slice — então a autorização declarativa não é reforçada aqui. Esses
 * testes cobrem o mapeamento HTTP/serialização; a aplicação real do {@code @PreAuthorize} fica
 * fora do escopo do teste de unidade de controller.
 */
@WebMvcTest(CGrupoProduto.class)
class CGrupoProdutoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SGrupoProduto grupoProdutoService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "admin@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void getGrupos_deveRetornar200() throws Exception {
        GrupoProdutoRespostaDto dto = new GrupoProdutoRespostaDto();
        dto.setId(1L);
        dto.setNome("Rações");
        when(grupoProdutoService.listar(null, null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/grupos-produto")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Rações"));
    }

    @Test
    void postGrupo_deveRetornar200() throws Exception {
        GrupoProdutoCadastroDto dto = new GrupoProdutoCadastroDto();
        dto.setNome("Medicamentos");
        dto.setCodigoPrefixo("02");
        dto.setCategoriaGrupo(EnTipoInsumo.MEDICAMENTO);
        dto.setNaturezaFinanceira(EnNaturezaFinanceira.GASTO);

        GrupoProdutoRespostaDto criado = new GrupoProdutoRespostaDto();
        criado.setId(2L);
        criado.setNome("Medicamentos");
        when(grupoProdutoService.criar(any(GrupoProdutoCadastroDto.class))).thenReturn(criado);

        mockMvc.perform(post("/api/grupos-produto")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void postGrupo_deveRetornar400_QuandoPrefixoInvalido() throws Exception {
        GrupoProdutoCadastroDto dto = new GrupoProdutoCadastroDto();
        dto.setNome("Medicamentos");
        dto.setCodigoPrefixo("ABC");
        dto.setCategoriaGrupo(EnTipoInsumo.MEDICAMENTO);
        dto.setNaturezaFinanceira(EnNaturezaFinanceira.GASTO);

        mockMvc.perform(post("/api/grupos-produto")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteGrupo_deveRetornarMensagem() throws Exception {
        when(grupoProdutoService.inativar(1L)).thenReturn("Grupo de produto inativado com sucesso");

        mockMvc.perform(delete("/api/grupos-produto/1")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.ADMINISTRADOR))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Grupo de produto inativado com sucesso"));
    }
}
