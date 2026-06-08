package br.com.gado.application.services;

import br.com.gado.application.dto.TarefaDTO;
import br.com.gado.domain.entities.EListasTarefas;
import br.com.gado.domain.entities.ETarefa;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IListasTarefas;
import br.com.gado.infrastructure.persistence.repositories.ITarefa;
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
class STarefaTest {

    @InjectMocks
    private STarefa sTarefa;

    @Mock
    private ITarefa tarefaInterface;

    @Mock
    private IListasTarefas listaTarefas;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    private ETarefa tarefaEntity;
    private EListasTarefas listaEntity;
    private TarefaDTO tarefaDto;

    private final Long TAREFA_ID = 1L;
    private final Long LISTA_ID = 10L;

    @BeforeEach
    void setUp() {
        listaEntity = new EListasTarefas();
        listaEntity.setId(LISTA_ID);

        tarefaEntity = new ETarefa();
        tarefaEntity.setId(TAREFA_ID);
        tarefaEntity.setDescricao("Alimentar o gado"); // CORRIGIDO: de setTitulo para setDescricao
        tarefaEntity.setStatusConclusao(false);
        tarefaEntity.setStatus(EnStatus.A);
        tarefaEntity.setListasTarefaId(listaEntity);

        tarefaDto = new TarefaDTO();
        tarefaDto.setId(TAREFA_ID);
        tarefaDto.setDescricao("Alimentar o gado"); // CORRIGIDO: de setTitulo para setDescricao
    }

    @Nested
    class CriarTarefaTests {

        @Test
        void deveCriarTarefaComSucesso() {
            when(listaTarefas.findById(LISTA_ID)).thenReturn(Optional.of(listaEntity));
            when(tarefaInterface.save(any(ETarefa.class))).thenReturn(tarefaEntity);

            TarefaDTO resultado = sTarefa.criarTarefa(tarefaDto, LISTA_ID);

            assertNotNull(resultado);
            assertEquals(TAREFA_ID, resultado.getId());
            verify(tarefaInterface, times(1)).save(any(ETarefa.class));
        }

        @Test
        void deveLancarExcecao_QuandoListaNaoEncontrada() {
            when(listaTarefas.findById(LISTA_ID)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                sTarefa.criarTarefa(tarefaDto, LISTA_ID);
            });

            assertEquals("Lista de tarefas não encontrada", exception.getMessage());
            verify(tarefaInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoOcorrerErroAoSalvar() {
            when(listaTarefas.findById(LISTA_ID)).thenReturn(Optional.of(listaEntity));
            when(tarefaInterface.save(any(ETarefa.class))).thenThrow(new RuntimeException("Erro no banco"));

            assertThrows(RuntimeException.class, () -> {
                sTarefa.criarTarefa(tarefaDto, LISTA_ID);
            });
        }
    }

    @Nested
    class BuscarTarefaPorIdTests {

        @Test
        void deveRetornarTarefa_QuandoIdExistir() {
            when(tarefaInterface.findById(TAREFA_ID)).thenReturn(Optional.of(tarefaEntity));

            TarefaDTO resultado = sTarefa.buscarTarefaPorId(TAREFA_ID);

            assertNotNull(resultado);
            assertEquals(TAREFA_ID, resultado.getId());
            verify(tarefaInterface, times(1)).findById(TAREFA_ID);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoTarefaNaoExistir() {
            when(tarefaInterface.findById(TAREFA_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                sTarefa.buscarTarefaPorId(TAREFA_ID);
            });
        }
    }

    @Nested
    class AtualizarTarefaTests {

        @Test
        void deveAtualizarTarefaComSucesso() {
            when(tarefaInterface.findById(TAREFA_ID)).thenReturn(Optional.of(tarefaEntity));
            when(tarefaInterface.save(any(ETarefa.class))).thenReturn(tarefaEntity);

            TarefaDTO resultado = sTarefa.atualizarTarefa(tarefaDto, TAREFA_ID);

            assertNotNull(resultado);
            assertTrue(modelMapper.getConfiguration().isSkipNullEnabled());
            verify(tarefaInterface, times(1)).save(tarefaEntity);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoTarefaInexistente() {
            when(tarefaInterface.findById(TAREFA_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                sTarefa.atualizarTarefa(tarefaDto, TAREFA_ID);
            });
            verify(tarefaInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoErroNoBancoAoSalvarAtualizacao() {
            when(tarefaInterface.findById(TAREFA_ID)).thenReturn(Optional.of(tarefaEntity));
            when(tarefaInterface.save(any(ETarefa.class))).thenThrow(new RuntimeException("Erro de persistência"));

            assertThrows(RuntimeException.class, () -> {
                sTarefa.atualizarTarefa(tarefaDto, TAREFA_ID);
            });
        }
    }

    @Nested
    class ExcluirTarefaTests {

        @Test
        void deveInativarTarefaComSucesso() {
            when(tarefaInterface.findById(TAREFA_ID)).thenReturn(Optional.of(tarefaEntity));
            when(tarefaInterface.save(any(ETarefa.class))).thenReturn(tarefaEntity);

            String resultado = sTarefa.excluirTarefa(TAREFA_ID);

            assertEquals("tarefa excluída com sucesso", resultado);
            assertEquals(EnStatus.I, tarefaEntity.getStatus());
            verify(tarefaInterface, times(1)).save(tarefaEntity);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoTentarExcluirInexistente() {
            when(tarefaInterface.findById(TAREFA_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                sTarefa.excluirTarefa(TAREFA_ID);
            });
            verify(tarefaInterface, never()).save(any());
        }

        @Test
        void deveRetornarMensagemDeErro_QuandoOcorrerExcecaoNoBanco() {
            when(tarefaInterface.findById(TAREFA_ID)).thenReturn(Optional.of(tarefaEntity));
            when(tarefaInterface.save(any(ETarefa.class))).thenThrow(new RuntimeException("Erro de integridade"));

            String resultado = sTarefa.excluirTarefa(TAREFA_ID);

            assertEquals("erro ao excluir tarefa", resultado);
        }
    }
}