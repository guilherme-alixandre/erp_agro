package br.com.gado.controllers;

import br.com.gado.application.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.application.dto.usuarioDto.UsuarioDto;
import br.com.gado.application.dto.usuarioDto.UsuarioLoginDto;
import br.com.gado.application.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.application.services.SUsuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CUsuarioTest {

    private MockMvc mockMvc;

    @Mock
    private SUsuario usuarioService;

    @InjectMocks
    private CUsuario cUsuario;

    private ObjectMapper objectMapper;
    private UsuarioDto usuarioDto;
    private final String emailTeste = "gabriel@gado.com";

    @BeforeEach
    void setUp() {
        // Inicializa o ambiente de teste do MVC injetando os mocks isoladamente
        mockMvc = MockMvcBuilders.standaloneSetup(cUsuario).build();
        objectMapper = new ObjectMapper();

        usuarioDto = new UsuarioDto();
        usuarioDto.setNome("Gabriel");
        usuarioDto.setEmail(emailTeste);
    }

    @Test
    void getUsuario_DeveRetornarUsuarioDto_QuandoEmailExistir() throws Exception {
        when(usuarioService.encontraPorEmail(emailTeste)).thenReturn(usuarioDto);

        mockMvc.perform(get("/api/usuarios/{email}", emailTeste))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Gabriel"))
                .andExpect(jsonPath("$.email").value(emailTeste));

        verify(usuarioService, times(1)).encontraPorEmail(emailTeste);
    }

    @Test
    void getUsuarios_DeveRetornarListaDeUsuarios_QuandoSucesso() throws Exception {
        ArrayList<UsuarioDto> lista = new ArrayList<>();
        lista.add(usuarioDto);

        when(usuarioService.buscarTodos()).thenReturn(lista);

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Gabriel"))
                .andExpect(jsonPath("$[0].email").value(emailTeste));

        verify(usuarioService, times(1)).buscarTodos();
    }

    @Test
    void postUsuario_DeveCriarUsuarioERetornarDto_QuandoDadosValidos() throws Exception {
        UsuarioCadastroDto cadastroDto = new UsuarioCadastroDto();
        cadastroDto.setNome("Gabriel");
        cadastroDto.setEmail(emailTeste);

        when(usuarioService.cadastra(any(UsuarioCadastroDto.class))).thenReturn(usuarioDto);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadastroDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Gabriel"))
                .andExpect(jsonPath("$.email").value(emailTeste));

        verify(usuarioService, times(1)).cadastra(any(UsuarioCadastroDto.class));
    }

    @Nested
    class LoginTests {

        @Test
        void login_DeveRetornarBadRequest_QuandoEmailForNuloOuVazio() throws Exception {
            UsuarioLoginDto loginDto = new UsuarioLoginDto();
            loginDto.setSenha("senha123"); // Email nulo aqui

            mockMvc.perform(post("/api/usuarios/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value("Informe e-mail e senha."));

            verify(usuarioService, never()).login(any());
        }

        @Test
        void login_DeveRetornarBadRequest_QuandoSenhaForNuloOuVazio() throws Exception {
            UsuarioLoginDto loginDto = new UsuarioLoginDto();
            loginDto.setEmail(emailTeste); // Senha nula aqui

            mockMvc.perform(post("/api/usuarios/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value("Informe e-mail e senha."));

            verify(usuarioService, never()).login(any());
        }

        @Test
        void login_DeveRetornarOk_QuandoCredenciaisForemCorretas() throws Exception {
            UsuarioLoginDto loginDto = new UsuarioLoginDto();
            loginDto.setEmail(emailTeste);
            loginDto.setSenha("senha123");

            when(usuarioService.login(any(UsuarioLoginDto.class))).thenReturn(usuarioDto);

            mockMvc.perform(post("/api/usuarios/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(emailTeste))
                    .andExpect(jsonPath("$.nome").value("Gabriel"));

            verify(usuarioService, times(1)).login(any(UsuarioLoginDto.class));
        }

        @Test
        void login_DeveRetornarUnauthorized_QuandoServiceLancarIllegalArgumentException() throws Exception {
            UsuarioLoginDto loginDto = new UsuarioLoginDto();
            loginDto.setEmail(emailTeste);
            loginDto.setSenha("senhaIncorreta");

            when(usuarioService.login(any(UsuarioLoginDto.class)))
                    .thenThrow(new IllegalArgumentException("Credenciais inválidas."));

            mockMvc.perform(post("/api/usuarios/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.mensagem").value("Credenciais inválidas."));

            verify(usuarioService, times(1)).login(any(UsuarioLoginDto.class));
        }
    }

    @Test
    void deleteUsuario_DeveRetornarMensagemConfirmacao_QuandoDeletado() throws Exception {
        when(usuarioService.deleta(emailTeste)).thenReturn("Usuário deletado com sucesso.");

        mockMvc.perform(delete("/api/usuarios/{email}", emailTeste))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuário deletado com sucesso."));

        verify(usuarioService, times(1)).deleta(emailTeste);
    }

    @Test
    void putUsuario_DeveAtualizarERetornarUsuarioDto_QuandoSucesso() throws Exception {
        UsuarioPutDto putDto = new UsuarioPutDto();
        putDto.setNome("Gabriel Alterado");

        UsuarioDto usuarioAlterado = new UsuarioDto();
        usuarioAlterado.setNome("Gabriel Alterado");
        usuarioAlterado.setEmail(emailTeste);

        when(usuarioService.altera(eq(emailTeste), any(UsuarioPutDto.class))).thenReturn(usuarioAlterado);

        mockMvc.perform(put("/api/usuarios/{email}", emailTeste)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Gabriel Alterado"));

        verify(usuarioService, times(1)).altera(eq(emailTeste), any(UsuarioPutDto.class));
    }
}