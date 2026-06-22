package br.com.gado.application.services;

import br.com.gado.application.dto.RegistroFinanceiroDTO;
import br.com.gado.domain.entities.ECategoria;
import br.com.gado.domain.entities.ERegistroFinanceiro;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.ICategoria;
import br.com.gado.infrastructure.persistence.repositories.IRegistroFinanceiro;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SRegistroFinanceiroTest {

    @InjectMocks
    private SRegistroFinanceiro sRegistroFinanceiro;

    @Mock
    private IRegistroFinanceiro registroFinanceiroInterface;

    @Mock
    private IUsuario usuarioInterface;

    @Mock
    private ICategoria categoriaInterface;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Configuration configuration;

    private RegistroFinanceiroDTO registroFinanceiroDTO;
    private ERegistroFinanceiro registroFinanceiroEntity;
    private EUsuario usuarioEntity;
    private ECategoria categoriaEntity;

    private final Long ID = 1L;

    @BeforeEach
    void setUp() {
        usuarioEntity = new EUsuario();
        usuarioEntity.setId(ID);
        usuarioEntity.setEmail("teste@email.com");
        usuarioEntity.setStatus(EnStatus.A);

        categoriaEntity = new ECategoria();
        categoriaEntity.setId(ID);

        registroFinanceiroEntity = new ERegistroFinanceiro();
        registroFinanceiroEntity.setId(ID);
        registroFinanceiroEntity.setUsuarioId(usuarioEntity);
        registroFinanceiroEntity.setCategoriaId(categoriaEntity);

        registroFinanceiroDTO = new RegistroFinanceiroDTO();
        registroFinanceiroDTO.setUsuarioId(usuarioEntity);
        registroFinanceiroDTO.setCategoriaId(categoriaEntity);
    }

    /** Stub reutilizado em todos os testes: usuário válido, sem perfil bloqueado. */
    private void stubUsuarioValido() {
        when(usuarioInterface.findByEmailAndStatus(usuarioEntity.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(usuarioEntity));
    }

    @Nested
    class CriarRegistroFinanceiroTests {

        @Test
        void criarRegistroFinanceiro_DeveSalvarComSucesso() {
            stubUsuarioValido();
            when(categoriaInterface.findById(ID)).thenReturn(Optional.of(categoriaEntity));
            when(modelMapper.map(registroFinanceiroDTO, ERegistroFinanceiro.class)).thenReturn(registroFinanceiroEntity);
            when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class))).thenReturn(registroFinanceiroEntity);
            when(modelMapper.map(registroFinanceiroEntity, RegistroFinanceiroDTO.class)).thenReturn(registroFinanceiroDTO);

            RegistroFinanceiroDTO response =
                    sRegistroFinanceiro.criarRegistroFinanceiro(usuarioEntity.getEmail(), registroFinanceiroDTO);

            assertNotNull(response);
            verify(registroFinanceiroInterface, times(1)).save(any(ERegistroFinanceiro.class));
        }

        @Test
        void criarRegistroFinanceiro_DeveLancarExcecao_QuandoUsuarioNaoExiste() {
            when(usuarioInterface.findByEmailAndStatus(usuarioEntity.getEmail(), EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> sRegistroFinanceiro.criarRegistroFinanceiro(usuarioEntity.getEmail(), registroFinanceiroDTO));
        }

        @Test
        void criarRegistroFinanceiro_DeveLancarExcecao_QuandoCategoriaNaoExiste() {
            stubUsuarioValido();
            when(categoriaInterface.findById(ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sRegistroFinanceiro.criarRegistroFinanceiro(usuarioEntity.getEmail(), registroFinanceiroDTO));
        }

        @Test
        void criarRegistroFinanceiro_DeveLancarExcecao_QuandoSaveFalhar() {
            stubUsuarioValido();
            when(categoriaInterface.findById(ID)).thenReturn(Optional.of(categoriaEntity));
            when(modelMapper.map(registroFinanceiroDTO, ERegistroFinanceiro.class)).thenReturn(registroFinanceiroEntity);
            when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class)))
                    .thenThrow(new RuntimeException("Erro ao salvar"));

            assertThrows(RuntimeException.class,
                    () -> sRegistroFinanceiro.criarRegistroFinanceiro(usuarioEntity.getEmail(), registroFinanceiroDTO));
        }
    }

    @Nested
    class BuscarRegistroFinanceiroTests {

        @Test
        void buscarRegistroFinanceiroPorId_DeveRetornarRegistro() {
            stubUsuarioValido();
            when(registroFinanceiroInterface.findById(ID)).thenReturn(Optional.of(registroFinanceiroEntity));
            when(modelMapper.map(registroFinanceiroEntity, RegistroFinanceiroDTO.class)).thenReturn(registroFinanceiroDTO);

            RegistroFinanceiroDTO response =
                    sRegistroFinanceiro.buscarRegistroFinanceiroPorId(usuarioEntity.getEmail(), ID);

            assertNotNull(response);
        }

        @Test
        void buscarRegistroFinanceiroPorId_DeveLancarExcecao() {
            stubUsuarioValido();
            when(registroFinanceiroInterface.findById(ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sRegistroFinanceiro.buscarRegistroFinanceiroPorId(usuarioEntity.getEmail(), ID));
        }
    }

    @Nested
    class AtualizarRegistroFinanceiroTests {

        @Test
        void atualizarRegistroFinanceiroPorId_DeveAtualizarComSucesso() {
            stubUsuarioValido();
            when(registroFinanceiroInterface.findById(ID)).thenReturn(Optional.of(registroFinanceiroEntity));
            when(modelMapper.getConfiguration()).thenReturn(configuration);
            when(configuration.setSkipNullEnabled(true)).thenReturn(configuration);
            doNothing().when(modelMapper).map(any(RegistroFinanceiroDTO.class), any(ERegistroFinanceiro.class));
            when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class))).thenReturn(registroFinanceiroEntity);
            when(modelMapper.map(registroFinanceiroEntity, RegistroFinanceiroDTO.class)).thenReturn(registroFinanceiroDTO);

            RegistroFinanceiroDTO response =
                    sRegistroFinanceiro.atualizarRegistroFinanceiroPorId(usuarioEntity.getEmail(), ID, registroFinanceiroDTO);

            assertNotNull(response);
            verify(registroFinanceiroInterface, times(1)).save(any(ERegistroFinanceiro.class));
        }

        @Test
        void atualizarRegistroFinanceiroPorId_DeveLancarExcecao() {
            stubUsuarioValido();
            when(registroFinanceiroInterface.findById(ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sRegistroFinanceiro.atualizarRegistroFinanceiroPorId(usuarioEntity.getEmail(), ID, registroFinanceiroDTO));
        }

        @Test
        void atualizarRegistroFinanceiroPorId_DeveLancarExcecao_QuandoSaveFalhar() {
            stubUsuarioValido();
            when(registroFinanceiroInterface.findById(ID)).thenReturn(Optional.of(registroFinanceiroEntity));
            when(modelMapper.getConfiguration()).thenReturn(configuration);
            when(configuration.setSkipNullEnabled(true)).thenReturn(configuration);
            doNothing().when(modelMapper).map(any(RegistroFinanceiroDTO.class), any(ERegistroFinanceiro.class));
            when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class)))
                    .thenThrow(new RuntimeException("Erro ao atualizar"));

            assertThrows(RuntimeException.class,
                    () -> sRegistroFinanceiro.atualizarRegistroFinanceiroPorId(usuarioEntity.getEmail(), ID, registroFinanceiroDTO));
        }
    }

    @Nested
    class ExcluirRegistroFinanceiroTests {

        @Test
        void excluirRegistroFinanceiroPorId_DeveExcluirComSucesso() {
            stubUsuarioValido();
            when(registroFinanceiroInterface.findById(ID)).thenReturn(Optional.of(registroFinanceiroEntity));
            when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class))).thenReturn(registroFinanceiroEntity);

            String response =
                    sRegistroFinanceiro.excluirRegistroFinanceiroPorId(usuarioEntity.getEmail(), ID);

            assertEquals("registro financeiro excluído com sucesso", response);
        }

        @Test
        void excluirRegistroFinanceiroPorId_DeveRetornarErro() {
            stubUsuarioValido();
            when(registroFinanceiroInterface.findById(ID)).thenReturn(Optional.of(registroFinanceiroEntity));
            when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class)))
                    .thenThrow(new RuntimeException());

            String response =
                    sRegistroFinanceiro.excluirRegistroFinanceiroPorId(usuarioEntity.getEmail(), ID);

            assertEquals("erro ao excluir registro financeiro", response);
        }

        @Test
        void excluirRegistroFinanceiroPorId_DeveLancarExcecao() {
            stubUsuarioValido();
            when(registroFinanceiroInterface.findById(ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sRegistroFinanceiro.excluirRegistroFinanceiroPorId(usuarioEntity.getEmail(), ID));
        }
    }

    @Nested
    class ValidaPermissaoTests {

        @Test
        void validaPermissao_DeveLancarExcecao_QuandoEmailForNulo() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sRegistroFinanceiro.validaPermissao(null));
            assertEquals("Informe o e-mail do usuário responsável pela operação.", ex.getMessage());
        }

        @Test
        void validaPermissao_DeveLancarExcecao_QuandoEmailForBlank() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sRegistroFinanceiro.validaPermissao("   "));
            assertEquals("Informe o e-mail do usuário responsável pela operação.", ex.getMessage());
        }

        @Test
        void validaPermissao_DeveLancarExcecao_QuandoUsuarioNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(usuarioEntity.getEmail(), EnStatus.A))
                    .thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sRegistroFinanceiro.validaPermissao(usuarioEntity.getEmail()));
            assertEquals("Usuário não encontrado.", ex.getMessage());
        }

        @Test
        void validaPermissao_DeveLancarExcecao_QuandoPerfilForCuidador() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
            when(usuarioInterface.findByEmailAndStatus(usuarioEntity.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sRegistroFinanceiro.validaPermissao(usuarioEntity.getEmail()));
            assertEquals("Cuidadores não têm acesso ao módulo financeiro.", ex.getMessage());
        }

        @Test
        void validaPermissao_DeveLancarExcecao_QuandoPerfilForCuidadorChefe() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
            when(usuarioInterface.findByEmailAndStatus(usuarioEntity.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sRegistroFinanceiro.validaPermissao(usuarioEntity.getEmail()));
            assertEquals("Cuidadores não têm acesso ao módulo financeiro.", ex.getMessage());
        }

        @Test
        void validaPermissao_DevePassar_QuandoPerfilForAdministrador() {
            usuarioEntity.setPerfil(EnPerfilUsuario.ADMINISTRADOR);
            when(usuarioInterface.findByEmailAndStatus(usuarioEntity.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            assertDoesNotThrow(() -> sRegistroFinanceiro.validaPermissao(usuarioEntity.getEmail()));
        }

        @Test
        void validaPermissao_DevePassar_QuandoPerfilForGerente() {
            usuarioEntity.setPerfil(EnPerfilUsuario.GERENTE);
            when(usuarioInterface.findByEmailAndStatus(usuarioEntity.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            assertDoesNotThrow(() -> sRegistroFinanceiro.validaPermissao(usuarioEntity.getEmail()));
        }

        @Test
        void validaPermissao_DevePassar_QuandoPerfilForFinanceiro() {
            usuarioEntity.setPerfil(EnPerfilUsuario.FINANCEIRO);
            when(usuarioInterface.findByEmailAndStatus(usuarioEntity.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            assertDoesNotThrow(() -> sRegistroFinanceiro.validaPermissao(usuarioEntity.getEmail()));
        }
    }

    @Nested
    class CriarRegistroFinanceiroAdicionaisTests {

        @Test
        void criarRegistroFinanceiro_DeveLancarExcecao_QuandoUsuarioInternoNaoExiste() {
            // validaPermissao passes, but the inner findByEmailAndStatus for registroFinanceiroDto.getUsuarioId fails
            stubUsuarioValido();
            // the DTO's usuarioId references a different email that doesn't exist
            EUsuario dtoUsuario = new EUsuario();
            dtoUsuario.setEmail("inexistente@email.com");
            dtoUsuario.setStatus(EnStatus.A);
            registroFinanceiroDTO.setUsuarioId(dtoUsuario);

            when(usuarioInterface.findByEmailAndStatus("inexistente@email.com", EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sRegistroFinanceiro.criarRegistroFinanceiro(usuarioEntity.getEmail(), registroFinanceiroDTO));
        }
    }
}
