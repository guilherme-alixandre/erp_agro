package br.com.gado.application.services;

import br.com.gado.application.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.application.dto.usuarioDto.UsuarioDto;
import br.com.gado.application.dto.usuarioDto.UsuarioLoginDto;
import br.com.gado.application.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SUsuarioTest {

    @Mock
    private IUsuario usuarioInterface;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Configuration configuration;

    @InjectMocks
    private SUsuario sUsuario;

    private EUsuario usuarioAtivo;
    private UsuarioDto usuarioDto;
    private final String emailTeste = "teste@gado.com";
    // Hash SHA-256 correspondente à string "senha123"
    private final String senhaHashTeste = "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3";

    @BeforeEach
    void setUp() {
        usuarioAtivo = new EUsuario();
        usuarioAtivo.setEmail(emailTeste);
        usuarioAtivo.setSenha(senhaHashTeste);
        usuarioAtivo.setStatus(EnStatus.A);

        usuarioDto = new UsuarioDto();
        usuarioDto.setEmail(emailTeste);
    }

    // ==========================================
    // TESTES: encontraPorEmail
    // ==========================================

    @Test
    void encontraPorEmail_DeveRetornarUsuarioDto_QuandoEmailExisteEAtivo() {
        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));
        when(modelMapper.map(usuarioAtivo, UsuarioDto.class)).thenReturn(usuarioDto);

        UsuarioDto resultado = sUsuario.encontraPorEmail("  " + emailTeste + "  ");

        assertNotNull(resultado);
        assertEquals(emailTeste, resultado.getEmail());
        verify(usuarioInterface, times(1)).findByEmailAndStatus(emailTeste, EnStatus.A);
    }

    @Test
    void encontraPorEmail_DeveLancarEntityNotFoundException_QuandoEmailNaoExiste() {
        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class, () -> {
            sUsuario.encontraPorEmail(emailTeste);
        });

        assertEquals("Usuário não encontrado.", excecao.getMessage());
    }

    // ==========================================
    // TESTES: buscarTodos
    // ==========================================

    @Test
    void buscarTodos_DeveRetornarListaDeUsuarios_QuandoExistiremUsuariosAtivos() {
        ArrayList<EUsuario> listaUsuarios = new ArrayList<>(Arrays.asList(usuarioAtivo));
        when(usuarioInterface.findAllByStatus(EnStatus.A)).thenReturn(listaUsuarios);
        when(modelMapper.map(any(EUsuario.class), eq(UsuarioDto.class))).thenReturn(usuarioDto);

        ArrayList<UsuarioDto> resultado = sUsuario.buscarTodos();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    void buscarTodos_DeveRetornarListaVazia_QuandoNaoHouverUsuariosAtivos() {
        when(usuarioInterface.findAllByStatus(EnStatus.A)).thenReturn(new ArrayList<>());

        ArrayList<UsuarioDto> resultado = sUsuario.buscarTodos();

        assertTrue(resultado.isEmpty());
    }

    // ==========================================
    // TESTES: cadastra
    // ==========================================

    @Test
    void cadastra_DeveLancarException_QuandoDtoForNulo() {
        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.cadastra(null));
        assertEquals("Dados de cadastro ausentes.", excecao.getMessage());
    }

    @Test
    void cadastra_DeveLancarException_QuandoNomeInvalido() {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome(" ");

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.cadastra(dto));
        assertEquals("Informe o nome.", excecao.getMessage());
    }

    @Test
    void cadastra_DeveLancarException_QuandoEmailInvalido() {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome("Nome");
        dto.setEmail("");

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.cadastra(dto));
        assertEquals("Informe o e-mail.", excecao.getMessage());
    }

    @Test
    void cadastra_DeveLancarException_QuandoSenhaInvalida() {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome("Nome");
        dto.setEmail(emailTeste);
        dto.setSenha(null);

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.cadastra(dto));
        assertEquals("Informe a senha.", excecao.getMessage());
    }

    @Test
    void cadastra_DeveLancarException_QuandoPerfilInvalido() {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome("Nome");
        dto.setEmail(emailTeste);
        dto.setSenha("senha123");
        dto.setPerfil(null);

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.cadastra(dto));
        assertEquals("Informe o perfil.", excecao.getMessage());
    }

    @Test
    void cadastra_DeveLancarException_QuandoEmailJaCadastrado() {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome("Nome");
        dto.setEmail(emailTeste);
        dto.setSenha("senha123");
        dto.setPerfil(EnPerfilUsuario.ADMINISTRADOR);

        when(usuarioInterface.existsByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(true);

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.cadastra(dto));
        assertEquals("E-mail já cadastrado.", excecao.getMessage());
    }

    @Test
    void cadastra_DeveSalvarERetornarUsuarioDto_QuandoDadosForemValidos() {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome("Nome");
        dto.setEmail("  " + emailTeste + "  ");
        dto.setSenha("senha123");
        dto.setPerfil(EnPerfilUsuario.ADMINISTRADOR);

        when(usuarioInterface.existsByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(false);
        when(modelMapper.map(dto, EUsuario.class)).thenReturn(usuarioAtivo);
        when(usuarioInterface.save(any(EUsuario.class))).thenReturn(usuarioAtivo);
        when(modelMapper.map(usuarioAtivo, UsuarioDto.class)).thenReturn(usuarioDto);

        UsuarioDto resultado = sUsuario.cadastra(dto);

        assertNotNull(resultado);
        verify(usuarioInterface, times(1)).save(any(EUsuario.class));
    }

    // ==========================================
    // TESTES: deleta (Inativar)
    // ==========================================

    @Test
    void deleta_DeveLancarEntityNotFoundException_QuandoUsuarioNaoExistir() {
        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class, () -> sUsuario.deleta(emailTeste));
        assertEquals("usuário não encontrado ou inativo", excecao.getMessage());
    }

    @Test
    void deleta_DeveRetornarMensagemSucesso_QuandoInativadoComExito() {
        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));
        when(usuarioInterface.save(usuarioAtivo)).thenReturn(usuarioAtivo);

        String resultado = sUsuario.deleta(emailTeste);

        assertEquals("Usuário inativado com sucesso", resultado);
        assertEquals(EnStatus.I, usuarioAtivo.getStatus());
    }

    @Test
    void deleta_DeveRetornarMensagemErro_QuandoOcorrerExcecaoNoSave() {
        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));
        when(usuarioInterface.save(usuarioAtivo)).thenThrow(new RuntimeException("Erro de banco"));

        String resultado = sUsuario.deleta(emailTeste);

        assertEquals("Erro ao inativar usuário!", resultado);
    }

    // ==========================================
    // TESTES: altera
    // ==========================================

    @Test
    void altera_DeveLancarEntityNotFoundException_QuandoUsuarioNaoEncontrado() {
        UsuarioPutDto dto = new UsuarioPutDto();
        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class, () -> sUsuario.altera(emailTeste, dto));
        assertEquals("Usuário não encontrado.", excecao.getMessage());
    }

    @Test
    void altera_DeveRetornarUsuarioDtoAtualizado_QuandoSucesso() {
        UsuarioPutDto dto = new UsuarioPutDto();
        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));
        when(modelMapper.getConfiguration()).thenReturn(configuration);

        // Simula a primeira chamada do map(dto, usuario) que apenas altera o estado interno
        doNothing().when(modelMapper).map(dto, usuarioAtivo);

        when(usuarioInterface.save(usuarioAtivo)).thenReturn(usuarioAtivo);
        when(modelMapper.map(usuarioAtivo, UsuarioDto.class)).thenReturn(usuarioDto);

        UsuarioDto resultado = sUsuario.altera(emailTeste, dto);

        assertNotNull(resultado);
        verify(configuration, times(1)).setSkipNullEnabled(true);
        verify(modelMapper, times(1)).map(dto, usuarioAtivo);
    }

    // ==========================================
    // TESTES: login
    // ==========================================

    @Test
    void login_DeveLancarException_QuandoDtoForNulo() {
        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.login(null));
        assertEquals("Informe e-mail e senha.", excecao.getMessage());
    }

    @Test
    void login_DeveLancarException_QuandoEmailForInvalido() {
        UsuarioLoginDto dto = new UsuarioLoginDto();
        dto.setEmail("");

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.login(dto));
        assertEquals("Informe o e-mail.", excecao.getMessage());
    }

    @Test
    void login_DeveLancarException_QuandoSenhaForInvalida() {
        UsuarioLoginDto dto = new UsuarioLoginDto();
        dto.setEmail(emailTeste);
        dto.setSenha(" ");

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.login(dto));
        assertEquals("Informe a senha.", excecao.getMessage());
    }

    @Test
    void login_DeveLancarException_QuandoUsuarioNaoForEncontrado() {
        UsuarioLoginDto dto = new UsuarioLoginDto();
        dto.setEmail(emailTeste);
        dto.setSenha("senha123");

        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.empty());

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.login(dto));
        assertEquals("Credenciais inválidas.", excecao.getMessage());
    }

    @Test
    void login_DeveLancarException_QuandoSenhaDoBancoForNula() {
        UsuarioLoginDto dto = new UsuarioLoginDto();
        dto.setEmail(emailTeste);
        dto.setSenha("senha123");

        usuarioAtivo.setSenha(null);
        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.login(dto));
        assertEquals("Credenciais inválidas.", excecao.getMessage());
    }

    @Test
    void login_DeveLancarException_QuandoSenhaHashNaoConferir() {
        UsuarioLoginDto dto = new UsuarioLoginDto();
        dto.setEmail(emailTeste);
        dto.setSenha("senhaIncorreta");

        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.login(dto));
        assertEquals("Credenciais inválidas.", excecao.getMessage());
    }

    @Test
    void login_DeveRetornarUsuarioDto_QuandoSenhaHashConferir() {
        UsuarioLoginDto dto = new UsuarioLoginDto();
        dto.setEmail(emailTeste);
        dto.setSenha("senha123");

        usuarioAtivo.setSenha("55a5e9e78207b4df8699d60886fa070079463547b095d1a05bc719bb4e6cd251");

        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));
        when(modelMapper.map(usuarioAtivo, UsuarioDto.class)).thenReturn(usuarioDto);

        UsuarioDto resultado = sUsuario.login(dto);

        assertNotNull(resultado);
        verify(usuarioInterface, never()).save(any(EUsuario.class));
    }

    @Test
    void login_DeveLancarException_QuandoSenhaTextoPuroNaoConferir() {
        UsuarioLoginDto dto = new UsuarioLoginDto();
        dto.setEmail(emailTeste);
        dto.setSenha("senhaDigitada");

        usuarioAtivo.setSenha("outraSenhaTextoPuro"); // Não passa na regex SHA-256
        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> sUsuario.login(dto));
        assertEquals("Credenciais inválidas.", excecao.getMessage());
    }

    @Test
    void login_DeveAtualizarParaHashERetornarUsuarioDto_QuandoSenhaTextoPuroConferir() {
        UsuarioLoginDto dto = new UsuarioLoginDto();
        dto.setEmail(emailTeste);
        dto.setSenha("senha123");

        usuarioAtivo.setSenha("senha123"); // Texto puro idêntico (Compatibilidade)
        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));
        when(modelMapper.map(usuarioAtivo, UsuarioDto.class)).thenReturn(usuarioDto);

        UsuarioDto resultado = sUsuario.login(dto);

        assertNotNull(resultado);
        // Modificado para o valor hash real gerado a partir de "senha123"
        assertEquals("55a5e9e78207b4df8699d60886fa070079463547b095d1a05bc719bb4e6cd251", usuarioAtivo.getSenha());
        verify(usuarioInterface, times(1)).save(usuarioAtivo);
    }

    // ==========================================
    // FORÇAR COBERTURA: Bloco Catch do sha256Hex
    // ==========================================

    @Test
    void altera_DeveHashearNovaSenha_QuandoSenhaForInformada() {
        UsuarioPutDto dto = new UsuarioPutDto();
        dto.setSenha("novaSenha123");

        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));
        when(modelMapper.getConfiguration()).thenReturn(configuration);

        doNothing().when(modelMapper).map(dto, usuarioAtivo);
        when(usuarioInterface.save(usuarioAtivo)).thenReturn(usuarioAtivo);
        when(modelMapper.map(usuarioAtivo, UsuarioDto.class)).thenReturn(usuarioDto);

        UsuarioDto resultado = sUsuario.altera(emailTeste, dto);

        assertNotNull(resultado);
        // After altera the entity password should be a 64-char hex hash (not "novaSenha123")
        assertNotEquals("novaSenha123", usuarioAtivo.getSenha());
        assertEquals(64, usuarioAtivo.getSenha().length());
    }

    // ==========================================
    // TESTES: altera - senha nula ou blank
    // ==========================================

    @Test
    void altera_NaoDeveHashearSenha_QuandoSenhaForNula() {
        UsuarioPutDto dto = new UsuarioPutDto();
        dto.setSenha(null);

        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));
        when(modelMapper.getConfiguration()).thenReturn(configuration);
        doNothing().when(modelMapper).map(dto, usuarioAtivo);
        when(usuarioInterface.save(usuarioAtivo)).thenReturn(usuarioAtivo);
        when(modelMapper.map(usuarioAtivo, UsuarioDto.class)).thenReturn(usuarioDto);

        UsuarioDto resultado = sUsuario.altera(emailTeste, dto);

        assertNotNull(resultado);
        // senha permanece inalterada (o hash inicial) pois novaSenha era null
        assertEquals(senhaHashTeste, usuarioAtivo.getSenha());
    }

    @Test
    void altera_NaoDeveHashearSenha_QuandoSenhaForBlank() {
        UsuarioPutDto dto = new UsuarioPutDto();
        dto.setSenha("   ");

        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));
        when(modelMapper.getConfiguration()).thenReturn(configuration);
        doNothing().when(modelMapper).map(dto, usuarioAtivo);
        when(usuarioInterface.save(usuarioAtivo)).thenReturn(usuarioAtivo);
        when(modelMapper.map(usuarioAtivo, UsuarioDto.class)).thenReturn(usuarioDto);

        UsuarioDto resultado = sUsuario.altera(emailTeste, dto);

        assertNotNull(resultado);
        // senha permanece inalterada pois novaSenha era blank
        assertEquals(senhaHashTeste, usuarioAtivo.getSenha());
    }

    // ==========================================
    // TESTES: login - senha em branco no banco
    // ==========================================

    @Test
    void login_DeveLancarException_QuandoSenhaDoBancoForBlank() {
        UsuarioLoginDto dto = new UsuarioLoginDto();
        dto.setEmail(emailTeste);
        dto.setSenha("senha123");

        usuarioAtivo.setSenha("   "); // blank
        when(usuarioInterface.findByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(Optional.of(usuarioAtivo));

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class,
                () -> sUsuario.login(dto));
        assertEquals("Credenciais inválidas.", excecao.getMessage());
    }

    // ==========================================
    // TESTES: login - email nulo / senha nula
    // ==========================================

    @Test
    void login_DeveLancarException_QuandoEmailForNulo() {
        UsuarioLoginDto dto = new UsuarioLoginDto();
        dto.setEmail(null);
        dto.setSenha("qualquerCoisa");

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class,
                () -> sUsuario.login(dto));
        assertEquals("Informe o e-mail.", excecao.getMessage());
    }

    @Test
    void login_DeveLancarException_QuandoSenhaForNula() {
        UsuarioLoginDto dto = new UsuarioLoginDto();
        dto.setEmail(emailTeste);
        dto.setSenha(null);

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class,
                () -> sUsuario.login(dto));
        assertEquals("Informe a senha.", excecao.getMessage());
    }

    // ==========================================
    // TESTES: cadastra - nome nulo
    // ==========================================

    @Test
    void cadastra_DeveLancarException_QuandoNomeForNulo() {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome(null);

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class,
                () -> sUsuario.cadastra(dto));
        assertEquals("Informe o nome.", excecao.getMessage());
    }

    @Test
    void cadastra_DeveLancarException_QuandoEmailForNulo() {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome("Nome");
        dto.setEmail(null);

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class,
                () -> sUsuario.cadastra(dto));
        assertEquals("Informe o e-mail.", excecao.getMessage());
    }

    @Test
    void cadastra_DeveLancarException_QuandoSenhaForBlank() {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome("Nome");
        dto.setEmail(emailTeste);
        dto.setSenha("   ");

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class,
                () -> sUsuario.cadastra(dto));
        assertEquals("Informe a senha.", excecao.getMessage());
    }

    @Test
    void sha256Hex_DeveLancarRuntimeException_QuandoAlgoritmoNaoExistir() {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome("Nome");
        dto.setEmail(emailTeste);
        dto.setSenha("senha123");
        dto.setPerfil(EnPerfilUsuario.ADMINISTRADOR);

        when(usuarioInterface.existsByEmailAndStatus(emailTeste, EnStatus.A)).thenReturn(false);
        // Garante que o mapper não retorne nulo antes de estourar a exceção do digest
        when(modelMapper.map(dto, EUsuario.class)).thenReturn(usuarioAtivo);

        try (MockedStatic<MessageDigest> mockedDigest = mockStatic(MessageDigest.class)) {
            mockedDigest.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new RuntimeException("Erro crítico simulado"));

            RuntimeException excecao = assertThrows(RuntimeException.class, () -> sUsuario.cadastra(dto));
            assertEquals("Falha ao processar senha.", excecao.getMessage());
        }
    }
}