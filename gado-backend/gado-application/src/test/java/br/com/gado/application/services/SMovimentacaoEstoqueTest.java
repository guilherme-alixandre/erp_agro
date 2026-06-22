package br.com.gado.application.services;

import br.com.gado.application.dto.MovimentacaoEstoqueDTO;
import br.com.gado.domain.entities.EMovimentacaoEstoque;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IMovimentacaoEstoque;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SMovimentacaoEstoqueTest {

    @InjectMocks
    private SMovimentacaoEstoque sMovimentacaoEstoque;

    @Mock
    private IMovimentacaoEstoque movimentacaoEstoqueInterface;

    private ModelMapper modelMapper;

    private MovimentacaoEstoqueDTO dto;
    private EMovimentacaoEstoque entity;

    private final Long ID_GENERICO = 1L;

    @BeforeEach
    void setUp() {

        modelMapper = new ModelMapper();

        sMovimentacaoEstoque =
                new SMovimentacaoEstoque(
                        movimentacaoEstoqueInterface,
                        modelMapper
                );

        dto = new MovimentacaoEstoqueDTO();
        dto.setId(ID_GENERICO);
        dto.setStatus(EnStatus.A);

        entity = new EMovimentacaoEstoque();
        entity.setId(ID_GENERICO);
        entity.setStatus(EnStatus.A);
    }

    @Nested
    class CriarMovimentacaoTests {

        @Test
        void criarMovimentacao_DeveSalvarComSucesso() {

            when(movimentacaoEstoqueInterface.save(any(EMovimentacaoEstoque.class)))
                    .thenReturn(entity);

            MovimentacaoEstoqueDTO response =
                    sMovimentacaoEstoque.criarMovimentacaoEstoque(dto);

            assertNotNull(response);
            assertEquals(ID_GENERICO, response.getId());

            verify(movimentacaoEstoqueInterface, times(1))
                    .save(any(EMovimentacaoEstoque.class));
        }

        @Test
        void criarMovimentacao_DeveLancarExcecao_QuandoSaveFalhar() {

            when(movimentacaoEstoqueInterface.save(any(EMovimentacaoEstoque.class)))
                    .thenThrow(new RuntimeException("Erro ao salvar"));

            assertThrows(RuntimeException.class,
                    () -> sMovimentacaoEstoque.criarMovimentacaoEstoque(dto));
        }
    }

    @Nested
    class BuscarMovimentacaoTests {

        @Test
        void buscarPorId_DeveRetornarMovimentacao() {

            when(movimentacaoEstoqueInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.of(entity));

            MovimentacaoEstoqueDTO response =
                    sMovimentacaoEstoque.buscarMovimentacaoEstoquePorId(ID_GENERICO);

            assertNotNull(response);
            assertEquals(ID_GENERICO, response.getId());
        }

        @Test
        void buscarPorId_DeveLancarExcecao_QuandoNaoEncontrado() {

            when(movimentacaoEstoqueInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sMovimentacaoEstoque.buscarMovimentacaoEstoquePorId(ID_GENERICO));
        }
    }

    @Nested
    class AtualizarMovimentacaoTests {

        @Test
        void atualizarMovimentacao_DeveAtualizarComSucesso() {

            MovimentacaoEstoqueDTO updateDto =
                    new MovimentacaoEstoqueDTO();

            updateDto.setStatus(EnStatus.I);

            when(movimentacaoEstoqueInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.of(entity));

            when(movimentacaoEstoqueInterface.save(any(EMovimentacaoEstoque.class)))
                    .thenReturn(entity);

            MovimentacaoEstoqueDTO response =
                    sMovimentacaoEstoque.atualizarMovimentacaoEstoquePorId(
                            ID_GENERICO,
                            updateDto
                    );

            assertNotNull(response);

            verify(movimentacaoEstoqueInterface, times(1))
                    .save(any(EMovimentacaoEstoque.class));
        }

        @Test
        void atualizarMovimentacao_DeveLancarExcecao_QuandoNaoEncontrado() {

            when(movimentacaoEstoqueInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sMovimentacaoEstoque.atualizarMovimentacaoEstoquePorId(
                            ID_GENERICO,
                            dto
                    ));
        }

        @Test
        void atualizarMovimentacao_DeveLancarExcecao_QuandoSaveFalhar() {

            when(movimentacaoEstoqueInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.of(entity));

            when(movimentacaoEstoqueInterface.save(any(EMovimentacaoEstoque.class)))
                    .thenThrow(new RuntimeException("Erro ao atualizar"));

            assertThrows(RuntimeException.class,
                    () -> sMovimentacaoEstoque.atualizarMovimentacaoEstoquePorId(
                            ID_GENERICO,
                            dto
                    ));
        }
    }

    @Nested
    class ExcluirMovimentacaoTests {

        @Test
        void excluirMovimentacao_DeveRealizarSoftDelete() {

            when(movimentacaoEstoqueInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.of(entity));

            when(movimentacaoEstoqueInterface.save(any(EMovimentacaoEstoque.class)))
                    .thenReturn(entity);

            String response =
                    sMovimentacaoEstoque.excluirMovimentacaoEstoquePorId(ID_GENERICO);

            assertEquals(
                    "movimentação de estoque excluída com sucesso",
                    response
            );

            assertEquals(EnStatus.I, entity.getStatus());

            verify(movimentacaoEstoqueInterface, times(1))
                    .save(entity);
        }

        @Test
        void excluirMovimentacao_DeveRetornarMensagemErro_QuandoSaveFalhar() {

            when(movimentacaoEstoqueInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.of(entity));

            when(movimentacaoEstoqueInterface.save(any(EMovimentacaoEstoque.class)))
                    .thenThrow(new RuntimeException());

            String response =
                    sMovimentacaoEstoque.excluirMovimentacaoEstoquePorId(ID_GENERICO);

            assertEquals(
                    "erro ao excluir movimentação de estoque",
                    response
            );
        }

        @Test
        void excluirMovimentacao_DeveLancarExcecao_QuandoNaoEncontrado() {

            when(movimentacaoEstoqueInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sMovimentacaoEstoque.excluirMovimentacaoEstoquePorId(ID_GENERICO));
        }
    }
}