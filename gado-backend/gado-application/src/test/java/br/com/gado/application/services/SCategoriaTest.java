package br.com.gado.application.services;

import br.com.gado.application.dto.CategoriaDTO;
import br.com.gado.domain.entities.ECategoria;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.ICategoria;
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
class SCategoriaTest {

    @InjectMocks
    private SCategoria sCategoria;

    @Mock
    private ICategoria categoriaInterface;

    // Usando @Spy para o ModelMapper aplicar a conversão real e configurações de skipNull
    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    private ECategoria categoriaEntity;
    private CategoriaDTO categoriaDTO;
    private final Long CATEGORIA_ID = 1L;

    @BeforeEach
    void setUp() {
        categoriaEntity = new ECategoria();
        categoriaEntity.setId(CATEGORIA_ID);
        // Presumindo que haja um atributo "nome" ou similar.
        // Se houver outros campos obrigatórios, preencha-os aqui.
        categoriaEntity.setStatus(EnStatus.A);

        categoriaDTO = new CategoriaDTO();
        // categoriaDTO.setNome("Corte");
    }

    @Nested
    class CriarCategoriaTests {
        @Test
        void deveCriarCategoriaERetornarDto_QuandoSucesso() {
            when(categoriaInterface.save(any(ECategoria.class))).thenReturn(categoriaEntity);

            CategoriaDTO resultado = sCategoria.criarCategoria(categoriaDTO);

            assertNotNull(resultado);
            verify(categoriaInterface, times(1)).save(any(ECategoria.class));
        }
    }

    @Nested
    class BuscarCategoriaPorIdTests {
        @Test
        void deveRetornarCategoriaDto_QuandoIdEStatusForemAtivos() {
            when(categoriaInterface.findByIdAndStatus(CATEGORIA_ID, EnStatus.A))
                    .thenReturn(Optional.of(categoriaEntity));

            CategoriaDTO resultado = sCategoria.buscarCategoriaPorId(CATEGORIA_ID);

            assertNotNull(resultado);
            verify(categoriaInterface, times(1)).findByIdAndStatus(CATEGORIA_ID, EnStatus.A);
        }

        @Test
        void deveLancarExcecao_QuandoCategoriaNaoExistirOuEstiverInativa() {
            when(categoriaInterface.findByIdAndStatus(CATEGORIA_ID, EnStatus.A))
                    .thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                sCategoria.buscarCategoriaPorId(CATEGORIA_ID);
            });

            assertEquals("Categoria não encontrada ou está inativa.", exception.getMessage());
        }
    }

    @Nested
    class AtualizarCategoriaPorIdTests {
        @Test
        void deveAtualizarCategoriaERetornarDto_QuandoSucesso() {
            when(categoriaInterface.findById(CATEGORIA_ID)).thenReturn(Optional.of(categoriaEntity));
            when(categoriaInterface.save(any(ECategoria.class))).thenReturn(categoriaEntity);

            CategoriaDTO resultado = sCategoria.atualizarCategoriaPorId(categoriaDTO, CATEGORIA_ID);

            assertNotNull(resultado);
            assertTrue(modelMapper.getConfiguration().isSkipNullEnabled()); // Verifica config do ModelMapper
            verify(categoriaInterface, times(1)).save(categoriaEntity);
        }

        @Test
        void deveLancarExcecao_AoTentarAtualizarCategoriaInexistente() {
            when(categoriaInterface.findById(CATEGORIA_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                sCategoria.atualizarCategoriaPorId(categoriaDTO, CATEGORIA_ID);
            });

            verify(categoriaInterface, never()).save(any());
        }

        @Test
        void deveRepassarExcecao_QuandoErroAoSalvarAtualizacao_ParaTestarOCatchBlock() {
            when(categoriaInterface.findById(CATEGORIA_ID)).thenReturn(Optional.of(categoriaEntity));

            // Simulando um erro no banco de dados para forçar a entrada no catch block
            RuntimeException dbError = new RuntimeException("Erro de Banco de Dados");
            when(categoriaInterface.save(any(ECategoria.class))).thenThrow(dbError);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                sCategoria.atualizarCategoriaPorId(categoriaDTO, CATEGORIA_ID);
            });

            assertEquals("Erro de Banco de Dados", exception.getMessage());
            verify(categoriaInterface, times(1)).save(categoriaEntity);
        }
    }

    @Nested
    class ExcluirCategoriaTests {
        @Test
        void deveExcluirLogicamenteERetornarMensagem_QuandoSucesso() {
            when(categoriaInterface.findById(CATEGORIA_ID)).thenReturn(Optional.of(categoriaEntity));
            when(categoriaInterface.save(any(ECategoria.class))).thenReturn(categoriaEntity);

            String mensagem = sCategoria.excluirCategoria(CATEGORIA_ID);

            assertEquals("categoria excluida com sucesso", mensagem);
            assertEquals(EnStatus.I, categoriaEntity.getStatus()); // Garante que o status foi alterado para inativo
            verify(categoriaInterface, times(1)).save(categoriaEntity);
        }

        @Test
        void deveLancarExcecao_AoTentarExcluirCategoriaInexistente() {
            when(categoriaInterface.findById(CATEGORIA_ID)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                sCategoria.excluirCategoria(CATEGORIA_ID);
            });

            assertEquals("Categoria não encontrada com o ID: " + CATEGORIA_ID, exception.getMessage());
            verify(categoriaInterface, never()).save(any());
        }

        @Test
        void deveRetornarMensagemDeErro_QuandoErroAoSalvarExclusao_ParaTestarOCatchBlock() {
            when(categoriaInterface.findById(CATEGORIA_ID)).thenReturn(Optional.of(categoriaEntity));

            // Simulando um erro no banco para testar o bloco catch e a geração do log de erro
            when(categoriaInterface.save(any(ECategoria.class))).thenThrow(new RuntimeException("Database timeout"));

            String mensagem = sCategoria.excluirCategoria(CATEGORIA_ID);

            // A lógica de negócio retorna uma string genérica em caso de erro no catch
            assertEquals("erro ao excluir categoria", mensagem);
            verify(categoriaInterface, times(1)).save(categoriaEntity);
        }
    }
}