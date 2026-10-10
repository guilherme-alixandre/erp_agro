package br.com.gado.integration;

import br.com.gado.GadoApplication;
import br.com.gado.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.services.SUsuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo completo de autenticação (sem mocks, contra H2, com o SecurityConfig real ativo via
 * @AutoConfigureMockMvc): login gera um JWT válido, o JWT dá acesso a um endpoint protegido, e a
 * ausência/invalidade do token é rejeitada com 401 pelo authenticationEntryPoint customizado.
 */
@SpringBootTest(classes = GadoApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SUsuario usuarioService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String criarUsuarioEEmail(String senha) {
        String email = "auth-it-" + System.nanoTime() + "@fazenda.com";
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome("Usuário IT");
        dto.setEmail(email);
        dto.setSenha(senha);
        dto.setPerfil(EnPerfilUsuario.CUIDADOR);
        usuarioService.cadastra(dto);
        return email;
    }

    @Test
    void deveLogarEAcessarEndpointProtegido_ComTokenValido() throws Exception {
        String email = criarUsuarioEEmail("senha123");

        String loginJson = objectMapper.writeValueAsString(java.util.Map.of("email", email, "senha", "senha123"));

        String respostaLogin = mockMvc.perform(post("/api/usuarios/login")
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(respostaLogin).get("token").asText();

        mockMvc.perform(get("/api/tarefas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar401_QuandoSemToken() throws Exception {
        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void deveRetornar401_QuandoTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/tarefas")
                        .header("Authorization", "Bearer token-completamente-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar400_QuandoLoginComSenhaErrada() throws Exception {
        String email = criarUsuarioEEmail("senhaCorreta");

        String loginJson = objectMapper.writeValueAsString(java.util.Map.of("email", email, "senha", "senhaErrada"));

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginEndpoint_devePermitirAcesso_SemAutenticacaoPrevia() throws Exception {
        // /api/usuarios/login é o único endpoint com permitAll no SecurityConfig — confirma que
        // uma requisição sem token não é bloqueada por 401 antes mesmo de chegar ao controller.
        String loginJson = objectMapper.writeValueAsString(java.util.Map.of("email", "inexistente@fazenda.com", "senha", "x"));

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }
}
