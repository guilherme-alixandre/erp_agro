package br.com.gado.application.services;

import br.com.gado.application.dto.VacinacaoDTO;
import br.com.gado.domain.entities.EVacinacao;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IVacinacao;
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
class SVacinacaoTest {

    @InjectMocks
    private SVacinacao sVacinacao;

    @Mock
    private IVacinacao vacinacaoInterface;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    private EVacinacao vacinacaoEntity;
    private VacinacaoDTO vacinacaoDto;

    private final Long VACINACAO_ID = 1L;

    @BeforeEach
    void setUp() {
        vacinacaoEntity = new EVacinacao();
        vacinacaoEntity.setId(VACINACAO_ID);
        vacinacaoEntity.setStatus(EnStatus.A);

        vacinacaoDto = new VacinacaoDTO();
        vacinacaoDto.setId(VACINACAO_ID);
    }

    @Nested
    class CriarVacinacaoTests {

        @Test
        void deveCriarVacinacaoComSucesso() {
            when(vacinacaoInterface.save(any(EVacinacao.class))).thenReturn(vacinacaoEntity);

            VacinacaoDTO resultado = sVacinacao.criarVacinacao(vacinacaoDto);

            assertNotNull(resultado);
            assertEquals(VACINACAO_ID, resultado.getId());
            verify(vacinacaoInterface, times(1)).save(any(EVacinacao.class));
        }

        @Test
        void deveLancarExcecao_QuandoErroAoSalvar() {
            when(vacinacaoInterface.save(any(EVacinacao.class))).thenThrow(new RuntimeException("Erro de banco"));

            assertThrows(RuntimeException.class, () -> sVacinacao.criarVacinacao(vacinacaoDto));
        }
    }

    @Nested
    class BuscarVacinacaoPorIdTests {

        @Test
        void deveRetornarVacinacao_QuandoIdExistir() {
            when(vacinacaoInterface.findById(VACINACAO_ID)).thenReturn(Optional.of(vacinacaoEntity));

            VacinacaoDTO resultado = sVacinacao.buscarVacinacaoPorId(VACINACAO_ID);

            assertNotNull(resultado);
            assertEquals(VACINACAO_ID, resultado.getId());
            verify(vacinacaoInterface, times(1)).findById(VACINACAO_ID);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoIdNaoExistir() {
            when(vacinacaoInterface.findById(VACINACAO_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> sVacinacao.buscarVacinacaoPorId(VACINACAO_ID));
        }
    }

    @Nested
    class AtualizarVacinacaoPorIdTests {

        @Test
        void deveAtualizarVacinacaoComSucesso() {
            when(vacinacaoInterface.findById(VACINACAO_ID)).thenReturn(Optional.of(vacinacaoEntity));
            when(vacinacaoInterface.save(any(EVacinacao.class))).thenReturn(vacinacaoEntity);

            VacinacaoDTO resultado = sVacinacao.atualizarVacinacaoPorId(VACINACAO_ID, vacinacaoDto);

            assertNotNull(resultado);
            assertTrue(modelMapper.getConfiguration().isSkipNullEnabled());
            verify(vacinacaoInterface, times(1)).save(vacinacaoEntity);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoInexistente() {
            when(vacinacaoInterface.findById(VACINACAO_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> sVacinacao.atualizarVacinacaoPorId(VACINACAO_ID, vacinacaoDto));
            verify(vacinacaoInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoErroAoSalvarAtualizacao() {
            when(vacinacaoInterface.findById(VACINACAO_ID)).thenReturn(Optional.of(vacinacaoEntity));
            when(vacinacaoInterface.save(any(EVacinacao.class))).thenThrow(new RuntimeException("Erro ao persistir"));

            assertThrows(RuntimeException.class, () -> sVacinacao.atualizarVacinacaoPorId(VACINACAO_ID, vacinacaoDto));
        }
    }

    @Nested
    class ExcluirVacinacaoPorIdTests {

        @Test
        void deveInativarVacinacaoComSucesso() {
            when(vacinacaoInterface.findById(VACINACAO_ID)).thenReturn(Optional.of(vacinacaoEntity));
            when(vacinacaoInterface.save(any(EVacinacao.class))).thenReturn(vacinacaoEntity);

            String resultado = sVacinacao.excluirVacinacaoPorId(VACINACAO_ID);

            assertEquals("vacinação excluída com sucesso", resultado);
            assertEquals(EnStatus.I, vacinacaoEntity.getStatus());
            verify(vacinacaoInterface, times(1)).save(vacinacaoEntity);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoTentarExcluirInexistente() {
            when(vacinacaoInterface.findById(VACINACAO_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> sVacinacao.excluirVacinacaoPorId(VACINACAO_ID));
            verify(vacinacaoInterface, never()).save(any());
        }

        @Test
        void deveRetornarMensagemDeErro_QuandoOcorrerExcecaoNoBanco() {
            when(vacinacaoInterface.findById(VACINACAO_ID)).thenReturn(Optional.of(vacinacaoEntity));
            when(vacinacaoInterface.save(any(EVacinacao.class))).thenThrow(new RuntimeException("Erro ao deletar"));

            String resultado = sVacinacao.excluirVacinacaoPorId(VACINACAO_ID);

            assertEquals("erro ao excluir vacinação", resultado);
        }
    }
}