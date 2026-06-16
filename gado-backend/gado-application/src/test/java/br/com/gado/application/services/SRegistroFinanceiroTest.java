package br.com.gado.application.services;

import br.com.gado.application.dto.RegistroFinanceiroDTO;
import br.com.gado.domain.entities.ECategoria;
import br.com.gado.domain.entities.ERegistroFinanceiro;
import br.com.gado.domain.entities.EUsuario;
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

    @Nested
    class CriarRegistroFinanceiroTests {

        @Test
        void criarRegistroFinanceiro_DeveSalvarComSucesso() {

            when(usuarioInterface.findByEmailAndStatus(
                    usuarioEntity.getEmail(),
                    usuarioEntity.getStatus()
            )).thenReturn(Optional.of(usuarioEntity));

            when(categoriaInterface.findById(ID))
                    .thenReturn(Optional.of(categoriaEntity));

            when(modelMapper.map(registroFinanceiroDTO, ERegistroFinanceiro.class))
                    .thenReturn(registroFinanceiroEntity);

            when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class)))
                    .thenReturn(registroFinanceiroEntity);

            when(modelMapper.map(registroFinanceiroEntity, RegistroFinanceiroDTO.class))
                    .thenReturn(registroFinanceiroDTO);

            RegistroFinanceiroDTO response =
                    sRegistroFinanceiro.criarRegistroFinanceiro(registroFinanceiroDTO);

            assertNotNull(response);

            verify(registroFinanceiroInterface, times(1))
                    .save(any(ERegistroFinanceiro.class));
        }

        @Test
        void criarRegistroFinanceiro_DeveLancarExcecao_QuandoUsuarioNaoExiste() {

            when(usuarioInterface.findByEmailAndStatus(
                    usuarioEntity.getEmail(),
                    usuarioEntity.getStatus()
            )).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sRegistroFinanceiro.criarRegistroFinanceiro(registroFinanceiroDTO));
        }

        @Test
        void criarRegistroFinanceiro_DeveLancarExcecao_QuandoCategoriaNaoExiste() {

            when(usuarioInterface.findByEmailAndStatus(
                    usuarioEntity.getEmail(),
                    usuarioEntity.getStatus()
            )).thenReturn(Optional.of(usuarioEntity));

            when(categoriaInterface.findById(ID))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sRegistroFinanceiro.criarRegistroFinanceiro(registroFinanceiroDTO));
        }
    }

    @Nested
    class BuscarRegistroFinanceiroTests {

        @Test
        void buscarRegistroFinanceiroPorId_DeveRetornarRegistro() {

            when(registroFinanceiroInterface.findById(ID))
                    .thenReturn(Optional.of(registroFinanceiroEntity));

            when(modelMapper.map(registroFinanceiroEntity, RegistroFinanceiroDTO.class))
                    .thenReturn(registroFinanceiroDTO);

            RegistroFinanceiroDTO response =
                    sRegistroFinanceiro.buscarRegistroFinanceiroPorId(ID);

            assertNotNull(response);
        }

        @Test
        void buscarRegistroFinanceiroPorId_DeveLancarExcecao() {

            when(registroFinanceiroInterface.findById(ID))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sRegistroFinanceiro.buscarRegistroFinanceiroPorId(ID));
        }
    }

    @Nested
    class AtualizarRegistroFinanceiroTests {

        @Test
        void atualizarRegistroFinanceiroPorId_DeveAtualizarComSucesso() {

            when(registroFinanceiroInterface.findById(ID))
                    .thenReturn(Optional.of(registroFinanceiroEntity));

            when(modelMapper.getConfiguration())
                    .thenReturn(configuration);

            when(configuration.setSkipNullEnabled(true))
                    .thenReturn(configuration);

            doNothing().when(modelMapper)
                    .map(any(RegistroFinanceiroDTO.class), any(ERegistroFinanceiro.class));

            when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class)))
                    .thenReturn(registroFinanceiroEntity);

            when(modelMapper.map(registroFinanceiroEntity, RegistroFinanceiroDTO.class))
                    .thenReturn(registroFinanceiroDTO);

            RegistroFinanceiroDTO response =
                    sRegistroFinanceiro.atualizarRegistroFinanceiroPorId(ID, registroFinanceiroDTO);

            assertNotNull(response);

            verify(registroFinanceiroInterface, times(1))
                    .save(any(ERegistroFinanceiro.class));
        }

        @Test
        void atualizarRegistroFinanceiroPorId_DeveLancarExcecao() {

            when(registroFinanceiroInterface.findById(ID))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sRegistroFinanceiro.atualizarRegistroFinanceiroPorId(ID, registroFinanceiroDTO));
        }
    }

    @Nested
    class ExcluirRegistroFinanceiroTests {

        @Test
        void excluirRegistroFinanceiroPorId_DeveExcluirComSucesso() {

            when(registroFinanceiroInterface.findById(ID))
                    .thenReturn(Optional.of(registroFinanceiroEntity));

            when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class)))
                    .thenReturn(registroFinanceiroEntity);

            String response =
                    sRegistroFinanceiro.excluirRegistroFinanceiroPorId(ID);

            assertEquals(
                    "registro financeiro excluído com sucesso",
                    response
            );
        }

        @Test
        void excluirRegistroFinanceiroPorId_DeveRetornarErro() {

            when(registroFinanceiroInterface.findById(ID))
                    .thenReturn(Optional.of(registroFinanceiroEntity));

            when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class)))
                    .thenThrow(new RuntimeException());

            String response =
                    sRegistroFinanceiro.excluirRegistroFinanceiroPorId(ID);

            assertEquals(
                    "erro ao excluir registro financeiro",
                    response
            );
        }

        @Test
        void excluirRegistroFinanceiroPorId_DeveLancarExcecao() {

            when(registroFinanceiroInterface.findById(ID))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sRegistroFinanceiro.excluirRegistroFinanceiroPorId(ID));
        }
    }
}