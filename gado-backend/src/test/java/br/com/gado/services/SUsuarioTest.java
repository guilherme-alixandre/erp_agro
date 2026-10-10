package br.com.gado.services;

import br.com.gado.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.dto.usuarioDto.UsuarioDto;
import br.com.gado.dto.usuarioDto.UsuarioLoginDto;
import br.com.gado.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SUsuarioTest {

    @InjectMocks
    private SUsuario sUsuario;

    @Mock private IUsuario usuarioInterface;
    @Mock private PasswordEncoder passwordEncoder;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @AfterEach
    void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class EncontraPorEmailTests {

        @Test
        void deveRetornarUsuario_QuandoEncontrado() {
            EUsuario usuario = new EUsuario();
            usuario.setEmail("cuidador@fazenda.com");
            usuario.setNome("Cuidador");
            when(usuarioInterface.findByEmailAndStatus("cuidador@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.of(usuario));

            UsuarioDto resultado = sUsuario.encontraPorEmail("cuidador@fazenda.com");

            assertThat(resultado.getNome()).isEqualTo("Cuidador");
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus("fantasma@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sUsuario.encontraPorEmail("fantasma@fazenda.com"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class CadastraTests {

        @Test
        void deveCadastrarUsuario_CodificandoSenha() {
            when(usuarioInterface.existsByEmailAndStatus("novo@fazenda.com", EnStatus.A)).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("hash-bcrypt");
            when(usuarioInterface.save(any(EUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

            UsuarioCadastroDto dto = new UsuarioCadastroDto();
            dto.setNome("Novo Usuário");
            dto.setEmail("novo@fazenda.com");
            dto.setSenha("senha123");
            dto.setPerfil(EnPerfilUsuario.CUIDADOR);

            UsuarioDto resultado = sUsuario.cadastra(dto);

            assertThat(resultado.getNome()).isEqualTo("Novo Usuário");
            verify(usuarioInterface).save(argThat(u -> "hash-bcrypt".equals(u.getSenha())));
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioJaExiste() {
            when(usuarioInterface.existsByEmailAndStatus("existente@fazenda.com", EnStatus.A)).thenReturn(true);

            UsuarioCadastroDto dto = new UsuarioCadastroDto();
            dto.setEmail("existente@fazenda.com");

            assertThatThrownBy(() -> sUsuario.cadastra(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("usuário já existe");
        }
    }

    @Nested
    class LoginTests {

        private UsuarioLoginDto dto(String email, String senha) {
            UsuarioLoginDto dto = new UsuarioLoginDto();
            dto.setEmail(email);
            dto.setSenha(senha);
            return dto;
        }

        @Test
        void deveLogar_QuandoSenhaBCryptConfere() {
            EUsuario usuario = new EUsuario();
            usuario.setEmail("cuidador@fazenda.com");
            usuario.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1");
            when(usuarioInterface.findByEmailAndStatus("cuidador@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("senha123", usuario.getSenha())).thenReturn(true);

            UsuarioDto resultado = sUsuario.login(dto("cuidador@fazenda.com", "senha123"));

            assertThat(resultado.getEmail()).isEqualTo("cuidador@fazenda.com");
            verify(usuarioInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoSenhaBCryptNaoConfere() {
            EUsuario usuario = new EUsuario();
            usuario.setEmail("cuidador@fazenda.com");
            usuario.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1");
            when(usuarioInterface.findByEmailAndStatus("cuidador@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("senhaErrada", usuario.getSenha())).thenReturn(false);

            assertThatThrownBy(() -> sUsuario.login(dto("cuidador@fazenda.com", "senhaErrada")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Credenciais inválidas");
        }

        @Test
        void deveLogarEFazerUpgradeParaBCrypt_QuandoSenhaEraSha256() {
            // sha256("senha-antiga") calculado fora do service, não depende da lógica testada
            String shaHex = "c19efc696611b356c11cedd48a647c3d0b7e75277c46dc2bf34e5fd1ea45bb3e";
            EUsuario usuario = new EUsuario();
            usuario.setEmail("cuidador@fazenda.com");
            usuario.setSenha(shaHex);
            when(usuarioInterface.findByEmailAndStatus("cuidador@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.of(usuario));
            when(passwordEncoder.encode("senha-antiga")).thenReturn("hash-bcrypt-novo");
            when(usuarioInterface.save(any(EUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

            sUsuario.login(dto("cuidador@fazenda.com", "senha-antiga"));

            assertThat(usuario.getSenha()).isEqualTo("hash-bcrypt-novo");
            verify(usuarioInterface).save(usuario);
        }

        @Test
        void deveLogarEFazerUpgradeParaBCrypt_QuandoSenhaEraTextoPuro() {
            EUsuario usuario = new EUsuario();
            usuario.setEmail("cuidador@fazenda.com");
            usuario.setSenha("123mudar");
            when(usuarioInterface.findByEmailAndStatus("cuidador@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.of(usuario));
            when(passwordEncoder.encode("123mudar")).thenReturn("hash-bcrypt-novo");
            when(usuarioInterface.save(any(EUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

            sUsuario.login(dto("cuidador@fazenda.com", "123mudar"));

            assertThat(usuario.getSenha()).isEqualTo("hash-bcrypt-novo");
        }

        @Test
        void deveLancarExcecao_QuandoTextoPuroNaoConfere() {
            EUsuario usuario = new EUsuario();
            usuario.setEmail("cuidador@fazenda.com");
            usuario.setSenha("123mudar");
            when(usuarioInterface.findByEmailAndStatus("cuidador@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.of(usuario));

            assertThatThrownBy(() -> sUsuario.login(dto("cuidador@fazenda.com", "errada")))
                    .isInstanceOf(IllegalArgumentException.class);
            verify(usuarioInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoEmailOuSenhaEmBranco() {
            assertThatThrownBy(() -> sUsuario.login(dto("", "senha")))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> sUsuario.login(dto("email@fazenda.com", "")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus("fantasma@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sUsuario.login(dto("fantasma@fazenda.com", "senha")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Credenciais inválidas");
        }
    }

    @Nested
    class DeletaTests {

        @Test
        void deveInativarUsuario() {
            EUsuario usuario = new EUsuario();
            usuario.setEmail("cuidador@fazenda.com");
            when(usuarioInterface.findByEmailAndStatus("cuidador@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.of(usuario));
            when(usuarioInterface.save(any(EUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

            String resultado = sUsuario.deleta("cuidador@fazenda.com");

            assertThat(resultado).isEqualTo("Usuário inativado com sucesso");
            assertThat(usuario.getStatus()).isEqualTo(EnStatus.I);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus("fantasma@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sUsuario.deleta("fantasma@fazenda.com"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class AlteraTests {

        @Test
        void devePermitirAlterarNome_SemMudarPerfil() {
            EUsuario usuario = new EUsuario();
            usuario.setEmail("cuidador@fazenda.com");
            usuario.setNome("Nome antigo");
            when(usuarioInterface.findByEmailAndStatus("cuidador@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.of(usuario));
            when(usuarioInterface.save(any(EUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

            UsuarioPutDto dto = new UsuarioPutDto();
            dto.setNome("Nome novo");

            UsuarioDto resultado = sUsuario.altera("cuidador@fazenda.com", dto);

            assertThat(resultado.getNome()).isEqualTo("Nome novo");
        }

        @Test
        void devePermitirAlterarPerfil_QuandoChamadorEhAdministrador() {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "admin@fazenda.com", null, List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))));

            EUsuario usuario = new EUsuario();
            usuario.setEmail("cuidador@fazenda.com");
            usuario.setPerfil(EnPerfilUsuario.CUIDADOR);
            when(usuarioInterface.findByEmailAndStatus("cuidador@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.of(usuario));
            when(usuarioInterface.save(any(EUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

            UsuarioPutDto dto = new UsuarioPutDto();
            dto.setPerfil(EnPerfilUsuario.GERENTE);

            UsuarioDto resultado = sUsuario.altera("cuidador@fazenda.com", dto);

            assertThat(resultado.getPerfil()).isEqualTo(EnPerfilUsuario.GERENTE);
        }

        @Test
        void deveNegarAlterarPerfil_QuandoChamadorNaoEhAdministrador() {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "cuidador@fazenda.com", null, List.of(new SimpleGrantedAuthority("ROLE_CUIDADOR"))));

            EUsuario usuario = new EUsuario();
            usuario.setEmail("cuidador@fazenda.com");
            when(usuarioInterface.findByEmailAndStatus("cuidador@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.of(usuario));

            UsuarioPutDto dto = new UsuarioPutDto();
            dto.setPerfil(EnPerfilUsuario.GERENTE);

            assertThatThrownBy(() -> sUsuario.altera("cuidador@fazenda.com", dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Apenas administradores");
        }

        @Test
        void deveCodificarNovaSenha_QuandoInformada() {
            EUsuario usuario = new EUsuario();
            usuario.setEmail("cuidador@fazenda.com");
            when(usuarioInterface.findByEmailAndStatus("cuidador@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.of(usuario));
            when(passwordEncoder.encode("novaSenha123")).thenReturn("hash-nova-senha");
            when(usuarioInterface.save(any(EUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

            UsuarioPutDto dto = new UsuarioPutDto();
            dto.setSenha("novaSenha123");

            sUsuario.altera("cuidador@fazenda.com", dto);

            assertThat(usuario.getSenha()).isEqualTo("hash-nova-senha");
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoUsuarioNaoExiste() {
            when(usuarioInterface.findByEmailAndStatus("fantasma@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sUsuario.altera("fantasma@fazenda.com", new UsuarioPutDto()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class BuscarTodosTests {

        @Test
        void deveRetornarListaVazia_QuandoNenhumUsuarioAtivo() {
            when(usuarioInterface.findAllByStatus(EnStatus.A)).thenReturn(new ArrayList<>());

            assertThat(sUsuario.buscarTodos()).isEmpty();
        }

        @Test
        void deveListarUsuariosAtivos() {
            EUsuario usuario = new EUsuario();
            usuario.setEmail("cuidador@fazenda.com");
            when(usuarioInterface.findAllByStatus(EnStatus.A)).thenReturn(new ArrayList<>(List.of(usuario)));

            assertThat(sUsuario.buscarTodos()).hasSize(1);
        }
    }
}
