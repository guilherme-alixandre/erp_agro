package br.com.gado.application.services;

import br.com.gado.application.dto.SetorDto;
import br.com.gado.domain.entities.ESetor;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.ISetor;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
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
    private IUsuario usuarioInterface;

    private ESetor setorEntity;
    private SetorDto setorDto;
    private EUsuario usuarioEntity;

    private final Long ID = 1L;
    private final String EMAIL_VALIDO = "usuario@gado.com";

    @BeforeEach
    void setUp() {
        usuarioEntity = new EUsuario();
        usuarioEntity.setId(99L);
        usuarioEntity.setNome("João Silva");
        usuarioEntity.setEmail(EMAIL_VALIDO);

        setorEntity = new ESetor();
        setorEntity.setId(ID);
        setorEntity.setNome("Pasto A");
        setorEntity.setCapacidadeMaxima(50);
        setorEntity.setStatus(EnStatus.A);
        setorEntity.setCriadoPor(usuarioEntity);

        setorDto = new SetorDto();
        setorDto.setId(ID);
        setorDto.setNome("Pasto A");
        setorDto.setCapacidadeMaxima(50);
    }

    @Nested
    class ProcurarPorIdTests {

        @Test
        void procuraPorId_DeveRetornarSetor() {
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));

            SetorDto response = sSetor.procuraPorId(ID);

            assertNotNull(response);
            assertEquals(ID, response.getId());
            assertEquals("Pasto A", response.getNome());
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
            // Cria explicitamente um ArrayList exigido pelo seu repositório
            ArrayList<ESetor> listaSetores = new ArrayList<>(List.of(setorEntity));

            when(setorInterface.findAllByStatus(EnStatus.A))
                    .thenReturn(listaSetores);

            ArrayList<SetorDto> response = sSetor.buscarTodos();

            assertNotNull(response);
            assertEquals(1, response.size());
            assertEquals("Pasto A", response.get(0).getNome());
        }

        @Test
        void buscarTodos_DeveRetornarListaVazia() {
            when(setorInterface.findAllByStatus(EnStatus.A))
                    .thenReturn(new ArrayList<>());

            ArrayList<SetorDto> response = sSetor.buscarTodos();

            assertNotNull(response);
            assertTrue(response.isEmpty());
        }
    }

    @Nested
    class CadastrarSetorTests {

        @Test
        void cadastrar_DeveSalvarSetorComSucesso() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            when(setorInterface.save(any(ESetor.class)))
                    .thenReturn(setorEntity);

            SetorDto response = sSetor.cadastra(setorDto, EMAIL_VALIDO);

            assertNotNull(response);
            assertEquals("Pasto A", response.getNome());
            verify(setorInterface, times(1)).save(any(ESetor.class));
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

            assertDoesNotThrow(() -> sSetor.deleta(ID));

            verify(setorInterface, times(1)).save(any(ESetor.class));
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
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));

            when(setorInterface.save(any(ESetor.class)))
                    .thenReturn(setorEntity);

            SetorDto response = sSetor.altera(ID, setorDto, EMAIL_VALIDO);

            assertNotNull(response);
            verify(setorInterface, times(1)).save(any(ESetor.class));
        }

        @Test
        void alterar_DeveLancarExcecao() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sSetor.altera(ID, setorDto, EMAIL_VALIDO));
        }
    }
}