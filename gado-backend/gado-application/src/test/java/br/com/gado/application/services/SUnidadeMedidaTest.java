package br.com.gado.application.services;

import br.com.gado.application.dto.UnidadeMedidaDTO;
import br.com.gado.domain.entities.EUnidadeMedida;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IUnidadeMedida;
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
class SUnidadeMedidaTest {

    @InjectMocks
    private SUnidadeMedida sUnidadeMedida;

    @Mock
    private IUnidadeMedida unidadeMedidaInterface;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    private EUnidadeMedida unidadeMedidaEntity;
    private UnidadeMedidaDTO unidadeMedidaDto;

    private final Long UNIDADE_ID = 1L;

    @BeforeEach
    void setUp() {
        unidadeMedidaEntity = new EUnidadeMedida();
        unidadeMedidaEntity.setId(UNIDADE_ID);
        unidadeMedidaEntity.setStatus(EnStatus.A);

        unidadeMedidaDto = new UnidadeMedidaDTO();
        unidadeMedidaDto.setId(UNIDADE_ID);
    }

    @Nested
    class CriarUnidadeMedidaTests {

        @Test
        void deveCriarUnidadeMedidaComSucesso() {
            when(unidadeMedidaInterface.save(any(EUnidadeMedida.class))).thenReturn(unidadeMedidaEntity);

            UnidadeMedidaDTO resultado = sUnidadeMedida.criarUnidadeMedida(unidadeMedidaDto);

            assertNotNull(resultado);
            assertEquals(UNIDADE_ID, resultado.getId());
            verify(unidadeMedidaInterface, times(1)).save(any(EUnidadeMedida.class));
        }

        @Test
        void deveLancarExcecao_QuandoOcorrerErroAoSalvar() {
            when(unidadeMedidaInterface.save(any(EUnidadeMedida.class))).thenThrow(new RuntimeException("Erro de banco"));

            assertThrows(RuntimeException.class, () -> {
                sUnidadeMedida.criarUnidadeMedida(unidadeMedidaDto);
            });
        }
    }

    @Nested
    class BuscarUnidadeMedidaPorIdTests {

        @Test
        void deveRetornarUnidadeMedida_QuandoIdExistir() {
            when(unidadeMedidaInterface.findById(UNIDADE_ID)).thenReturn(Optional.of(unidadeMedidaEntity));

            UnidadeMedidaDTO resultado = sUnidadeMedida.bucarUnidadeMedidaPorId(UNIDADE_ID);

            assertNotNull(resultado);
            assertEquals(UNIDADE_ID, resultado.getId());
            verify(unidadeMedidaInterface, times(1)).findById(UNIDADE_ID);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoIdNaoExistir() {
            when(unidadeMedidaInterface.findById(UNIDADE_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                sUnidadeMedida.bucarUnidadeMedidaPorId(UNIDADE_ID);
            });
        }
    }

    @Nested
    class AtualizarUnidadeMedidaTests {

        @Test
        void deveAtualizarUnidadeMedidaComSucesso() {
            when(unidadeMedidaInterface.findById(UNIDADE_ID)).thenReturn(Optional.of(unidadeMedidaEntity));
            when(unidadeMedidaInterface.save(any(EUnidadeMedida.class))).thenReturn(unidadeMedidaEntity);

            UnidadeMedidaDTO resultado = sUnidadeMedida.atualizarUnidadeMedida(UNIDADE_ID, unidadeMedidaDto);

            assertNotNull(resultado);
            assertTrue(modelMapper.getConfiguration().isSkipNullEnabled());
            verify(unidadeMedidaInterface, times(1)).save(unidadeMedidaEntity);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoInexistente() {
            when(unidadeMedidaInterface.findById(UNIDADE_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                sUnidadeMedida.atualizarUnidadeMedida(UNIDADE_ID, unidadeMedidaDto);
            });
            verify(unidadeMedidaInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoErroAoSalvarAtualizacao() {
            when(unidadeMedidaInterface.findById(UNIDADE_ID)).thenReturn(Optional.of(unidadeMedidaEntity));
            when(unidadeMedidaInterface.save(any(EUnidadeMedida.class))).thenThrow(new RuntimeException("Erro de persistência"));

            assertThrows(RuntimeException.class, () -> {
                sUnidadeMedida.atualizarUnidadeMedida(UNIDADE_ID, unidadeMedidaDto);
            });
        }
    }

    @Nested
    class ExcluirUnidadeMedidaTests {

        @Test
        void deveInativarUnidadeMedidaComSucesso() {
            when(unidadeMedidaInterface.findById(UNIDADE_ID)).thenReturn(Optional.of(unidadeMedidaEntity));
            when(unidadeMedidaInterface.save(any(EUnidadeMedida.class))).thenReturn(unidadeMedidaEntity);

            String resultado = sUnidadeMedida.excluirUnidadeMedida(UNIDADE_ID);

            assertEquals("unidade de medida excluída com sucesso", resultado);
            assertEquals(EnStatus.I, unidadeMedidaEntity.getStatus());
            verify(unidadeMedidaInterface, times(1)).save(unidadeMedidaEntity);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoTentarExcluirInexistente() {
            when(unidadeMedidaInterface.findById(UNIDADE_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                sUnidadeMedida.excluirUnidadeMedida(UNIDADE_ID);
            });
            verify(unidadeMedidaInterface, never()).save(any());
        }

        @Test
        void deveRetornarMensagemDeErro_QuandoOcorrerExcecaoNoBanco() {
            when(unidadeMedidaInterface.findById(UNIDADE_ID)).thenReturn(Optional.of(unidadeMedidaEntity));
            when(unidadeMedidaInterface.save(any(EUnidadeMedida.class))).thenThrow(new RuntimeException("Erro ao deletar"));

            String resultado = sUnidadeMedida.excluirUnidadeMedida(UNIDADE_ID);

            assertEquals("erro ao excluir unidade de medida", resultado);
        }
    }
}