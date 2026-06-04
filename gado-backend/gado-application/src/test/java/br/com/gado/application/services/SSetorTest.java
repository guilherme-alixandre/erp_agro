package br.com.gado.application.services;

import br.com.gado.application.dto.SetorDto;
import br.com.gado.domain.entities.ESetor;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.ISetor;
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

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SSetorTest {

    @InjectMocks
    private SSetor sSetor;

    @Mock
    private ISetor setorInterface;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Configuration configuration;

    private ESetor setorEntity;
    private SetorDto setorDto;

    private final Long ID = 1L;

    @BeforeEach
    void setUp() {

        setorEntity = new ESetor();
        setorEntity.setId(ID);
        setorEntity.setStatus(EnStatus.A);

        setorDto = new SetorDto();
        setorDto.setId(ID);
    }

    @Nested
    class ProcurarPorIdTests {

        @Test
        void procuraPorId_DeveRetornarSetor() {

            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));

            when(modelMapper.map(setorEntity, SetorDto.class))
                    .thenReturn(setorDto);

            SetorDto response =
                    sSetor.procuraPorId(ID);

            assertNotNull(response);
            assertEquals(ID, response.getId());
        }

        @Test
        void procuraPorId_DeveLancarExcecao() {

            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sSetor.procuraPorId(ID));
        }
    }

    @Nested
    class BuscarTodosTests {

        @Test
        void buscarTodos_DeveRetornarListaDeSetores() {

            ArrayList<ESetor> setores = new ArrayList<>();
            setores.add(setorEntity);

            when(setorInterface.findAllByStatus(EnStatus.A))
                    .thenReturn(setores);

            when(modelMapper.map(setorEntity, SetorDto.class))
                    .thenReturn(setorDto);

            ArrayList<SetorDto> response =
                    sSetor.buscarTodos();

            assertNotNull(response);
            assertEquals(1, response.size());
        }

        @Test
        void buscarTodos_DeveRetornarListaVazia() {

            when(setorInterface.findAllByStatus(EnStatus.A))
                    .thenReturn(new ArrayList<>());

            ArrayList<SetorDto> response =
                    sSetor.buscarTodos();

            assertNotNull(response);
            assertTrue(response.isEmpty());
        }
    }

    @Nested
    class CadastrarSetorTests {

        @Test
        void cadastrar_DeveSalvarSetorComSucesso() {

            when(modelMapper.map(setorDto, ESetor.class))
                    .thenReturn(setorEntity);

            when(setorInterface.save(any(ESetor.class)))
                    .thenReturn(setorEntity);

            when(modelMapper.map(setorEntity, SetorDto.class))
                    .thenReturn(setorDto);

            SetorDto response =
                    sSetor.cadastra(setorDto);

            assertNotNull(response);

            verify(setorInterface, times(1))
                    .save(any(ESetor.class));
        }
    }

    @Nested
    class DeletarSetorTests {

        @Test
        void deletar_DeveInativarSetor() {

            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));

            when(setorInterface.save(any(ESetor.class)))
                    .thenReturn(setorEntity);

            assertDoesNotThrow(() ->
                    sSetor.deleta(ID));

            verify(setorInterface, times(1))
                    .save(any(ESetor.class));

            assertEquals(EnStatus.I, setorEntity.getStatus());
        }

        @Test
        void deletar_DeveLancarExcecao() {

            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sSetor.deleta(ID));
        }
    }

    @Nested
    class AlterarSetorTests {

        @Test
        void alterar_DeveAtualizarSetorComSucesso() {

            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));

            when(modelMapper.getConfiguration())
                    .thenReturn(configuration);

            when(configuration.setSkipNullEnabled(true))
                    .thenReturn(configuration);

            doNothing().when(modelMapper)
                    .map(any(SetorDto.class), any(ESetor.class));

            when(setorInterface.save(any(ESetor.class)))
                    .thenReturn(setorEntity);

            when(modelMapper.map(setorEntity, SetorDto.class))
                    .thenReturn(setorDto);

            SetorDto response =
                    sSetor.altera(ID, setorDto);

            assertNotNull(response);

            verify(setorInterface, times(1))
                    .save(any(ESetor.class));
        }

        @Test
        void alterar_DeveLancarExcecao() {

            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sSetor.altera(ID, setorDto));
        }
    }
}