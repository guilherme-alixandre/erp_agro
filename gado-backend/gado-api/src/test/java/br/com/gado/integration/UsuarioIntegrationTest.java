package br.com.gado.integration;

import br.com.gado.GadoApplication;
import br.com.gado.application.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.application.dto.usuarioDto.UsuarioDto;
import br.com.gado.application.dto.usuarioDto.UsuarioLoginDto;
import br.com.gado.application.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.application.services.SUsuario;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = GadoApplication.class)
@ActiveProfiles("test")
@Transactional
class UsuarioIntegrationTest {

    @Autowired private SUsuario usuarioService;
    @Autowired private IUsuario usuarioRepository;

    private final String SENHA_PLAINTEXT = "senha123";
    // SHA-256 de "senha123"
    private final String SENHA_HASH = "55a5e9e78207b4df8699d60886fa070079463547b095d1a05bc719bb4e6cd251";

    @BeforeEach
    void setUp() {
        // cada teste cria os seus próprios dados; @Transactional garante rollback
    }

    // ── Happy Path: Cadastro ──────────────────────────────────────────────────

    @Nested
    class CadastroHappyPath {

        @Test
        void deveCadastrarUsuarioComSucesso_QuandoDadosValidos() {
            UsuarioCadastroDto dto = novoUsuarioDto(sufixo() + "@it.local", SENHA_PLAINTEXT);

            UsuarioDto resultado = usuarioService.cadastra(dto);

            assertThat(resultado.getEmail()).isEqualTo(dto.getEmail().trim());
            assertThat(resultado.getNome()).isEqualTo(dto.getNome());
            assertThat(usuarioRepository.existsByEmailAndStatus(dto.getEmail().trim(), EnStatus.A)).isTrue();
        }

        @Test
        void deveSalvarSenhaComoHash_NaoComotextoPuro() {
            String email = sufixo() + "@it.local";
            UsuarioCadastroDto dto = novoUsuarioDto(email, SENHA_PLAINTEXT);

            usuarioService.cadastra(dto);

            EUsuario salvo = usuarioRepository.findByEmailAndStatus(email, EnStatus.A).orElseThrow();
            assertThat(salvo.getSenha()).hasSize(64);
            assertThat(salvo.getSenha()).isNotEqualTo(SENHA_PLAINTEXT);
        }

        @Test
        void deveCadastrarUsuario_NormalizandoEmailComEspacos() {
            String emailComEspacos = "  usuario-" + sufixo() + "@it.local  ";
            String emailNormalizado = emailComEspacos.trim();
            UsuarioCadastroDto dto = novoUsuarioDto(emailComEspacos, SENHA_PLAINTEXT);

            UsuarioDto resultado = usuarioService.cadastra(dto);

            assertThat(resultado.getEmail()).isEqualTo(emailNormalizado);
        }
    }

    // ── Sad Path: Cadastro ────────────────────────────────────────────────────

    @Nested
    class CadastroSadPath {

        @Test
        void deveLancarExcecao_QuandoDtoForNulo() {
            assertThatThrownBy(() -> usuarioService.cadastra(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ausentes");
        }

        @Test
        void deveLancarExcecao_QuandoNomeForNulo() {
            UsuarioCadastroDto dto = new UsuarioCadastroDto();
            dto.setNome(null);
            assertThatThrownBy(() -> usuarioService.cadastra(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nome");
        }

        @Test
        void deveLancarExcecao_QuandoNomeForBlank() {
            UsuarioCadastroDto dto = new UsuarioCadastroDto();
            dto.setNome("   ");
            assertThatThrownBy(() -> usuarioService.cadastra(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nome");
        }

        @Test
        void deveLancarExcecao_QuandoEmailForNulo() {
            UsuarioCadastroDto dto = new UsuarioCadastroDto();
            dto.setNome("Nome");
            dto.setEmail(null);
            assertThatThrownBy(() -> usuarioService.cadastra(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("e-mail");
        }

        @Test
        void deveLancarExcecao_QuandoEmailForBlank() {
            UsuarioCadastroDto dto = new UsuarioCadastroDto();
            dto.setNome("Nome");
            dto.setEmail("   ");
            assertThatThrownBy(() -> usuarioService.cadastra(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("e-mail");
        }

        @Test
        void deveLancarExcecao_QuandoSenhaForNula() {
            UsuarioCadastroDto dto = new UsuarioCadastroDto();
            dto.setNome("Nome");
            dto.setEmail(sufixo() + "@it.local");
            dto.setSenha(null);
            assertThatThrownBy(() -> usuarioService.cadastra(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("senha");
        }

        @Test
        void deveLancarExcecao_QuandoSenhaForBlank() {
            UsuarioCadastroDto dto = new UsuarioCadastroDto();
            dto.setNome("Nome");
            dto.setEmail(sufixo() + "@it.local");
            dto.setSenha("   ");
            assertThatThrownBy(() -> usuarioService.cadastra(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("senha");
        }

        @Test
        void deveLancarExcecao_QuandoPerfilForNulo() {
            UsuarioCadastroDto dto = new UsuarioCadastroDto();
            dto.setNome("Nome");
            dto.setEmail(sufixo() + "@it.local");
            dto.setSenha(SENHA_PLAINTEXT);
            dto.setPerfil(null);
            assertThatThrownBy(() -> usuarioService.cadastra(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("perfil");
        }

        @Test
        void deveLancarExcecao_QuandoEmailJaCadastrado() {
            UsuarioCadastroDto dto = novoUsuarioDto(sufixo() + "@it.local", SENHA_PLAINTEXT);
            usuarioService.cadastra(dto);

            assertThatThrownBy(() -> usuarioService.cadastra(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("E-mail já cadastrado");
        }
    }

    // ── Happy Path: Busca ─────────────────────────────────────────────────────

    @Nested
    class BuscaHappyPath {

        @Test
        void deveEncontrarUsuarioPorEmail_QuandoExisteEAtivo() {
            UsuarioCadastroDto dto = novoUsuarioDto(sufixo() + "@it.local", SENHA_PLAINTEXT);
            usuarioService.cadastra(dto);

            UsuarioDto resultado = usuarioService.encontraPorEmail("  " + dto.getEmail() + "  ");

            assertThat(resultado.getEmail()).isEqualTo(dto.getEmail().trim());
        }

        @Test
        void deveBuscarTodos_RetornandoApenasPerfisAtivos() {
            UsuarioCadastroDto dto1 = novoUsuarioDto(sufixo() + "@it.local", SENHA_PLAINTEXT);
            UsuarioCadastroDto dto2 = novoUsuarioDto(sufixo() + "@it.local", SENHA_PLAINTEXT);
            usuarioService.cadastra(dto1);
            usuarioService.cadastra(dto2);

            ArrayList<UsuarioDto> resultado = usuarioService.buscarTodos();

            assertThat(resultado).extracting(UsuarioDto::getEmail)
                    .contains(dto1.getEmail().trim(), dto2.getEmail().trim());
        }

        @Test
        void deveBuscarTodos_RetornarListaVazia_QuandoNenhumAtivo() {
            // base vazia por rollback; cria e deleta um usuário
            UsuarioCadastroDto dto = novoUsuarioDto(sufixo() + "@it.local", SENHA_PLAINTEXT);
            usuarioService.cadastra(dto);
            usuarioService.deleta(dto.getEmail().trim());

            ArrayList<UsuarioDto> resultado = usuarioService.buscarTodos();

            assertThat(resultado).extracting(UsuarioDto::getEmail)
                    .doesNotContain(dto.getEmail().trim());
        }
    }

    // ── Sad Path: Busca ───────────────────────────────────────────────────────

    @Nested
    class BuscaSadPath {

        @Test
        void deveLancarExcecao_AoEncontrarEmailInexistente() {
            assertThatThrownBy(() -> usuarioService.encontraPorEmail("inexistente@it.local"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ── Happy Path: Alteração ─────────────────────────────────────────────────

    @Nested
    class AlteracaoHappyPath {

        @Test
        void deveAlterarNome_QuandoNomeInformado() {
            UsuarioCadastroDto dto = novoUsuarioDto(sufixo() + "@it.local", SENHA_PLAINTEXT);
            usuarioService.cadastra(dto);

            UsuarioPutDto putDto = new UsuarioPutDto();
            putDto.setNome("Nome Alterado IT");
            usuarioService.altera(dto.getEmail().trim(), putDto);

            UsuarioDto resultado = usuarioService.encontraPorEmail(dto.getEmail().trim());
            assertThat(resultado.getNome()).isEqualTo("Nome Alterado IT");
        }

        @Test
        void deveAlterarSenha_QuandoSenhaInformada() {
            UsuarioCadastroDto dto = novoUsuarioDto(sufixo() + "@it.local", SENHA_PLAINTEXT);
            usuarioService.cadastra(dto);

            UsuarioPutDto putDto = new UsuarioPutDto();
            putDto.setSenha("novaSenha456");
            usuarioService.altera(dto.getEmail().trim(), putDto);

            EUsuario atualizado = usuarioRepository.findByEmailAndStatus(dto.getEmail().trim(), EnStatus.A).orElseThrow();
            assertThat(atualizado.getSenha()).hasSize(64);
            assertThat(atualizado.getSenha()).isNotEqualTo(SENHA_HASH);
        }

        @Test
        void deveManterSenha_QuandoSenhaForNula() {
            String email = sufixo() + "@it.local";
            UsuarioCadastroDto dto = novoUsuarioDto(email, SENHA_PLAINTEXT);
            usuarioService.cadastra(dto);
            String hashAntes = usuarioRepository.findByEmailAndStatus(email, EnStatus.A).orElseThrow().getSenha();

            UsuarioPutDto putDto = new UsuarioPutDto();
            putDto.setSenha(null);
            usuarioService.altera(email, putDto);

            String hashDepois = usuarioRepository.findByEmailAndStatus(email, EnStatus.A).orElseThrow().getSenha();
            assertThat(hashDepois).isEqualTo(hashAntes);
        }

        @Test
        void deveManterSenha_QuandoSenhaForBlank() {
            String email = sufixo() + "@it.local";
            UsuarioCadastroDto dto = novoUsuarioDto(email, SENHA_PLAINTEXT);
            usuarioService.cadastra(dto);
            String hashAntes = usuarioRepository.findByEmailAndStatus(email, EnStatus.A).orElseThrow().getSenha();

            UsuarioPutDto putDto = new UsuarioPutDto();
            putDto.setSenha("   ");
            usuarioService.altera(email, putDto);

            String hashDepois = usuarioRepository.findByEmailAndStatus(email, EnStatus.A).orElseThrow().getSenha();
            assertThat(hashDepois).isEqualTo(hashAntes);
        }
    }

    // ── Sad Path: Alteração ───────────────────────────────────────────────────

    @Nested
    class AlteracaoSadPath {

        @Test
        void deveLancarExcecao_AoAlterarEmailInexistente() {
            UsuarioPutDto putDto = new UsuarioPutDto();
            assertThatThrownBy(() -> usuarioService.altera("inexistente@it.local", putDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ── Happy Path: Exclusão ──────────────────────────────────────────────────

    @Nested
    class ExclusaoHappyPath {

        @Test
        void deveInativarUsuario_QuandoEmailExiste() {
            UsuarioCadastroDto dto = novoUsuarioDto(sufixo() + "@it.local", SENHA_PLAINTEXT);
            usuarioService.cadastra(dto);

            String resultado = usuarioService.deleta(dto.getEmail().trim());

            assertThat(resultado).contains("sucesso");
            assertThat(usuarioRepository.findByEmailAndStatus(dto.getEmail().trim(), EnStatus.A)).isEmpty();
            assertThat(usuarioRepository.findAll()).anyMatch(u ->
                    u.getEmail().equals(dto.getEmail().trim()) && u.getStatus() == EnStatus.I);
        }
    }

    // ── Sad Path: Exclusão ────────────────────────────────────────────────────

    @Nested
    class ExclusaoSadPath {

        @Test
        void deveLancarExcecao_AoDeletarEmailInexistente() {
            assertThatThrownBy(() -> usuarioService.deleta("inexistente@it.local"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("não encontrado ou inativo");
        }
    }

    // ── Happy Path: Login ─────────────────────────────────────────────────────

    @Nested
    class LoginHappyPath {

        @Test
        void deveFazerLogin_QuandoSenhaHashConferir() {
            UsuarioCadastroDto dto = novoUsuarioDto(sufixo() + "@it.local", SENHA_PLAINTEXT);
            usuarioService.cadastra(dto);

            UsuarioLoginDto loginDto = new UsuarioLoginDto();
            loginDto.setEmail(dto.getEmail().trim());
            loginDto.setSenha(SENHA_PLAINTEXT);

            UsuarioDto resultado = usuarioService.login(loginDto);

            assertThat(resultado.getEmail()).isEqualTo(dto.getEmail().trim());
        }

        @Test
        void deveAtualizarSenhaParaHash_QuandoSenhaEstaEmTextoPuro() {
            // Salva diretamente no banco com senha em texto puro (simulando registro legado)
            String email = "legado-" + sufixo() + "@it.local";
            EUsuario legado = new EUsuario();
            legado.setNome("Legado IT");
            legado.setEmail(email);
            legado.setSenha(SENHA_PLAINTEXT);
            legado.setPerfil(EnPerfilUsuario.CUIDADOR);
            legado.setDataCadastro(LocalDateTime.now());
            usuarioRepository.save(legado);

            UsuarioLoginDto loginDto = new UsuarioLoginDto();
            loginDto.setEmail(email);
            loginDto.setSenha(SENHA_PLAINTEXT);

            usuarioService.login(loginDto);

            EUsuario atualizado = usuarioRepository.findByEmailAndStatus(email, EnStatus.A).orElseThrow();
            assertThat(atualizado.getSenha()).hasSize(64);
            assertThat(atualizado.getSenha()).isNotEqualTo(SENHA_PLAINTEXT);
        }
    }

    // ── Sad Path: Login ───────────────────────────────────────────────────────

    @Nested
    class LoginSadPath {

        @Test
        void deveLancarExcecao_QuandoDtoDeLoginForNulo() {
            assertThatThrownBy(() -> usuarioService.login(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("e-mail e senha");
        }

        @Test
        void deveLancarExcecao_QuandoEmailDeLoginForNulo() {
            UsuarioLoginDto dto = new UsuarioLoginDto();
            dto.setEmail(null);
            dto.setSenha(SENHA_PLAINTEXT);
            assertThatThrownBy(() -> usuarioService.login(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("e-mail");
        }

        @Test
        void deveLancarExcecao_QuandoEmailDeLoginForBlank() {
            UsuarioLoginDto dto = new UsuarioLoginDto();
            dto.setEmail("   ");
            dto.setSenha(SENHA_PLAINTEXT);
            assertThatThrownBy(() -> usuarioService.login(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("e-mail");
        }

        @Test
        void deveLancarExcecao_QuandoSenhaDeLoginForNula() {
            UsuarioLoginDto dto = new UsuarioLoginDto();
            dto.setEmail(sufixo() + "@it.local");
            dto.setSenha(null);
            assertThatThrownBy(() -> usuarioService.login(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("senha");
        }

        @Test
        void deveLancarExcecao_QuandoSenhaDeLoginForBlank() {
            UsuarioLoginDto dto = new UsuarioLoginDto();
            dto.setEmail(sufixo() + "@it.local");
            dto.setSenha("   ");
            assertThatThrownBy(() -> usuarioService.login(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("senha");
        }

        @Test
        void deveLancarExcecao_QuandoEmailNaoEncontradoNoLogin() {
            UsuarioLoginDto dto = new UsuarioLoginDto();
            dto.setEmail("inexistente@it.local");
            dto.setSenha(SENHA_PLAINTEXT);
            assertThatThrownBy(() -> usuarioService.login(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Credenciais inválidas");
        }

        @Test
        void deveLancarExcecao_QuandoSenhaArmazenadaForNula() {
            String email = "sem-senha-" + sufixo() + "@it.local";
            EUsuario u = new EUsuario();
            u.setNome("Sem Senha IT");
            u.setEmail(email);
            u.setSenha(null);
            u.setPerfil(EnPerfilUsuario.CUIDADOR);
            u.setDataCadastro(LocalDateTime.now());
            usuarioRepository.save(u);

            UsuarioLoginDto dto = new UsuarioLoginDto();
            dto.setEmail(email);
            dto.setSenha(SENHA_PLAINTEXT);
            assertThatThrownBy(() -> usuarioService.login(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Credenciais inválidas");
        }

        @Test
        void deveLancarExcecao_QuandoSenhaArmazenadaForBlank() {
            String email = "senha-blank-" + sufixo() + "@it.local";
            EUsuario u = new EUsuario();
            u.setNome("Senha Blank IT");
            u.setEmail(email);
            u.setSenha("   ");
            u.setPerfil(EnPerfilUsuario.CUIDADOR);
            u.setDataCadastro(LocalDateTime.now());
            usuarioRepository.save(u);

            UsuarioLoginDto dto = new UsuarioLoginDto();
            dto.setEmail(email);
            dto.setSenha(SENHA_PLAINTEXT);
            assertThatThrownBy(() -> usuarioService.login(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Credenciais inválidas");
        }

        @Test
        void deveLancarExcecao_QuandoSenhaHashNaoConferir() {
            UsuarioCadastroDto dto = novoUsuarioDto(sufixo() + "@it.local", SENHA_PLAINTEXT);
            usuarioService.cadastra(dto);

            UsuarioLoginDto loginDto = new UsuarioLoginDto();
            loginDto.setEmail(dto.getEmail().trim());
            loginDto.setSenha("senhaErrada");
            assertThatThrownBy(() -> usuarioService.login(loginDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Credenciais inválidas");
        }

        @Test
        void deveLancarExcecao_QuandoSenhaTextoPuroNaoConferir() {
            String email = "legado-wrong-" + sufixo() + "@it.local";
            EUsuario legado = new EUsuario();
            legado.setNome("Legado Wrong IT");
            legado.setEmail(email);
            legado.setSenha("senhaCorreta");
            legado.setPerfil(EnPerfilUsuario.CUIDADOR);
            legado.setDataCadastro(LocalDateTime.now());
            usuarioRepository.save(legado);

            UsuarioLoginDto loginDto = new UsuarioLoginDto();
            loginDto.setEmail(email);
            loginDto.setSenha("senhaErrada");
            assertThatThrownBy(() -> usuarioService.login(loginDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Credenciais inválidas");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UsuarioCadastroDto novoUsuarioDto(String email, String senha) {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome("Usuario IT " + sufixo());
        dto.setEmail(email);
        dto.setSenha(senha);
        dto.setPerfil(EnPerfilUsuario.CUIDADOR);
        return dto;
    }

    private static String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
