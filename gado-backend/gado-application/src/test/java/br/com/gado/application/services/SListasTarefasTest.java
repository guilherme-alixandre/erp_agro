package br.com.gado.application.services;

import br.com.gado.application.dto.ListasTarefasDTO;
import br.com.gado.domain.entities.EListasTarefas;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IListasTarefas;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SListasTarefasTest {

    @InjectMocks
    private SListasTarefas sListasTarefas;

    @Mock
    private IListasTarefas listasTarefasInterface;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    private EListasTarefas listaEntity;
    private ListasTarefasDTO listaDTO;
    private final Long LISTA_ID = 1L;

    @BeforeEach
    void setUp() {
        listaEntity = new EListasTarefas();
        listaEntity.setId(LISTA_ID);
        listaEntity.setStatus(EnStatus.A);
        // Se houver um título/nome na entidade, pode inicializar aqui. Ex: listaEntity.setTitulo("Tarefas do Dia");

        listaDTO = new ListasTarefasDTO();
        // listaDTO.setTitulo("Tarefas Atualizadas");
    }

    @Nested
    class CriarListaDeTarefasTests {
        @Test
        void deveCriarListaDeTarefasERetornarDto_QuandoSucesso() {
            when(listasTarefasInterface.save(any(EListasTarefas.class))).thenReturn(listaEntity);

            ListasTarefasDTO resultado = sListasTarefas.criarListaDeTarefas(listaDTO);

            assertNotNull(resultado);
            verify(listasTarefasInterface, times(1)).save(any(EListasTarefas.class));
        }
    }

    @Nested
    class BuscarListaDeTarefasPorIdTests {
        @Test
        void deveRetornarListaDeTarefasDto_QuandoIdForEncontrado() {
            when(listasTarefasInterface.findById(LISTA_ID)).thenReturn(Optional.of(listaEntity));

            ListasTarefasDTO resultado = sListasTarefas.buscarListaDeTarefasPorId(LISTA_ID);

            assertNotNull(resultado);
            verify(listasTarefasInterface, times(1)).findById(LISTA_ID);
        }

        @Test
        void deveLancarExcecao_QuandoListaDeTarefasNaoExistir() {
            when(listasTarefasInterface.findById(LISTA_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                sListasTarefas.buscarListaDeTarefasPorId(LISTA_ID);
            });
        }
    }

    @Nested
    class AtualizarListaDeTarefasPorIdTests {
        @Test
        void deveAtualizarListaDeTarefasERetornarDto_QuandoSucesso() {
            when(listasTarefasInterface.findById(LISTA_ID)).thenReturn(Optional.of(listaEntity));
            when(listasTarefasInterface.save(any(EListasTarefas.class))).thenReturn(listaEntity);

            ListasTarefasDTO resultado = sListasTarefas.atualizarListaDeTarefasPorId(listaDTO, LISTA_ID);

            assertNotNull(resultado);
            assertTrue(modelMapper.getConfiguration().isSkipNullEnabled());
            verify(listasTarefasInterface, times(1)).save(listaEntity);
        }

        @Test
        void deveLancarExcecao_AoTentarAtualizarListaInexistente() {
            when(listasTarefasInterface.findById(LISTA_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                sListasTarefas.atualizarListaDeTarefasPorId(listaDTO, LISTA_ID);
            });

            verify(listasTarefasInterface, never()).save(any());
        }

        @Test
        void deveRepassarExcecao_QuandoOcorrerErroAoSalvar_ParaCobrirCatchBlock() {
            when(listasTarefasInterface.findById(LISTA_ID)).thenReturn(Optional.of(listaEntity));

            // Força o erro no banco para cair no bloco catch e acionar o log.error
            RuntimeException dbError = new RuntimeException("Erro Inesperado no BD");
            when(listasTarefasInterface.save(any(EListasTarefas.class))).thenThrow(dbError);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                sListasTarefas.atualizarListaDeTarefasPorId(listaDTO, LISTA_ID);
            });

            assertEquals("Erro Inesperado no BD", exception.getMessage());
            verify(listasTarefasInterface, times(1)).save(listaEntity);
        }
    }

    @Nested
    class ExcluirListaDeTarefasTests {
        @Test
        void deveExcluirLogicamenteERetornarMensagem_QuandoSucesso() {
            when(listasTarefasInterface.findById(LISTA_ID)).thenReturn(Optional.of(listaEntity));
            when(listasTarefasInterface.save(any(EListasTarefas.class))).thenReturn(listaEntity);

            String mensagem = sListasTarefas.excluirListaDeTarefas(LISTA_ID);

            assertEquals("lista de tarefas excluída com sucesso", mensagem);
            assertEquals(EnStatus.I, listaEntity.getStatus()); // Verifica se o status inativou
            verify(listasTarefasInterface, times(1)).save(listaEntity);
        }

        @Test
        void deveLancarExcecao_AoTentarExcluirListaInexistente() {
            when(listasTarefasInterface.findById(LISTA_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                sListasTarefas.excluirListaDeTarefas(LISTA_ID);
            });

            verify(listasTarefasInterface, never()).save(any());
        }

        @Test
        void deveRetornarMensagemDeErro_QuandoOcorrerErroAoSalvarExclusao_ParaCobrirCatchBlock() {
            when(listasTarefasInterface.findById(LISTA_ID)).thenReturn(Optional.of(listaEntity));

            // Força o erro para acionar o bloco catch no método de exclusão
            when(listasTarefasInterface.save(any(EListasTarefas.class))).thenThrow(new RuntimeException("Timeout DB"));

            String mensagem = sListasTarefas.excluirListaDeTarefas(LISTA_ID);

            assertEquals("erro ao excluir lista de tarefas", mensagem);
            verify(listasTarefasInterface, times(1)).save(listaEntity);
        }
    }
}