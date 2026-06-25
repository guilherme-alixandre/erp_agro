package br.com.gado.application.services;

import br.com.gado.application.dto.TrasacaoDTO;
import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.EParceiro;
import br.com.gado.domain.entities.ETransacao;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.ILote;
import br.com.gado.infrastructure.persistence.repositories.IParceiro;
import br.com.gado.infrastructure.persistence.repositories.ITrasacao;
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
class STrasacaoTest {

    @InjectMocks
    private STrasacao sTrasacao;

    @Mock
    private ITrasacao trasacaoInterface;

    @Mock
    private IParceiro parceiroInterface;

    @Mock
    private ILote loteInterface;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    private ETransacao transacaoEntity;
    private EParceiro parceiroEntity;
    private ELote loteEntity;
    private TrasacaoDTO trasacaoDto;

    private final Long ID = 1L;

    @BeforeEach
    void setUp() {
        trasacaoDto = mock(TrasacaoDTO.class, RETURNS_DEEP_STUBS);

        parceiroEntity = new EParceiro();
        loteEntity = new ELote();

        transacaoEntity = new ETransacao();
        transacaoEntity.setId(ID);
        transacaoEntity.setStatus(EnStatus.A);
        transacaoEntity.setParceiro(parceiroEntity);
        transacaoEntity.setLote(loteEntity);

        // Usando lenient() para o Mockito não reclamar nos testes que não usam o DTO completo
        lenient().when(trasacaoDto.getParceiro().getCpfCnpj()).thenReturn("12345678900");
        lenient().when(trasacaoDto.getLote().getId()).thenReturn(ID);
    }

    @Nested
    class CriarTrasacaoTests {

        @Test
        void deveCriarTrasacaoComSucesso() {
            when(parceiroInterface.findByCpfCnpj(any())).thenReturn(Optional.of(parceiroEntity));
            when(loteInterface.findById(any())).thenReturn(Optional.of(loteEntity));

            when(modelMapper.map(trasacaoDto, ETransacao.class)).thenReturn(transacaoEntity);
            when(trasacaoInterface.save(any(ETransacao.class))).thenReturn(transacaoEntity);
            when(modelMapper.map(transacaoEntity, TrasacaoDTO.class)).thenReturn(trasacaoDto);

            TrasacaoDTO resultado = sTrasacao.criarTrasacao(trasacaoDto);

            assertNotNull(resultado);
            verify(trasacaoInterface, times(1)).save(any(ETransacao.class));
        }

        @Test
        void deveLancarExcecao_QuandoParceiroNaoEncontrado() {
            when(parceiroInterface.findByCpfCnpj(any())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> sTrasacao.criarTrasacao(trasacaoDto));
            verify(loteInterface, never()).findById(any());
            verify(trasacaoInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoLoteNaoEncontrado() {
            when(parceiroInterface.findByCpfCnpj(any())).thenReturn(Optional.of(parceiroEntity));
            when(loteInterface.findById(any())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> sTrasacao.criarTrasacao(trasacaoDto));
            verify(trasacaoInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoErroAoSalvarNoBanco() {
            when(parceiroInterface.findByCpfCnpj(any())).thenReturn(Optional.of(parceiroEntity));
            when(loteInterface.findById(any())).thenReturn(Optional.of(loteEntity));
            when(modelMapper.map(trasacaoDto, ETransacao.class)).thenReturn(transacaoEntity);
            when(trasacaoInterface.save(any(ETransacao.class))).thenThrow(new RuntimeException("Erro de banco"));

            assertThrows(RuntimeException.class, () -> sTrasacao.criarTrasacao(trasacaoDto));
        }
    }

    @Nested
    class BuscarTrasacaoPorIdTests {

        @Test
        void deveRetornarTrasacao_QuandoIdExistir() {
            when(trasacaoInterface.findById(ID)).thenReturn(Optional.of(transacaoEntity));
            when(modelMapper.map(transacaoEntity, TrasacaoDTO.class)).thenReturn(trasacaoDto);

            TrasacaoDTO resultado = sTrasacao.buscarTrasacaoPorId(ID);

            assertNotNull(resultado);
            verify(trasacaoInterface, times(1)).findById(ID);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoIdNaoExistir() {
            when(trasacaoInterface.findById(ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> sTrasacao.buscarTrasacaoPorId(ID));
        }
    }

    @Nested
    class AtualizarTrasacaoPorIdTests {

        @Test
        void deveAtualizarTrasacaoComSucesso() {
            when(trasacaoInterface.findById(ID)).thenReturn(Optional.of(transacaoEntity));

            doNothing().when(modelMapper).map(trasacaoDto, transacaoEntity);

            when(trasacaoInterface.save(any(ETransacao.class))).thenReturn(transacaoEntity);
            when(modelMapper.map(transacaoEntity, TrasacaoDTO.class)).thenReturn(trasacaoDto);

            TrasacaoDTO resultado = sTrasacao.atualizarTrasacaoPorId(ID, trasacaoDto);

            assertNotNull(resultado);
            assertTrue(modelMapper.getConfiguration().isSkipNullEnabled());
            verify(trasacaoInterface, times(1)).save(transacaoEntity);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoInexistente() {
            when(trasacaoInterface.findById(ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> sTrasacao.atualizarTrasacaoPorId(ID, trasacaoDto));
            verify(trasacaoInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoErroAoSalvarAtualizacao() {
            when(trasacaoInterface.findById(ID)).thenReturn(Optional.of(transacaoEntity));
            doNothing().when(modelMapper).map(trasacaoDto, transacaoEntity);
            when(trasacaoInterface.save(any(ETransacao.class))).thenThrow(new RuntimeException("Erro ao atualizar"));

            assertThrows(RuntimeException.class, () -> sTrasacao.atualizarTrasacaoPorId(ID, trasacaoDto));
        }
    }

    @Nested
    class ExcluirTrasacaoPorIdTests {

        @Test
        void deveExcluirComSucesso() {
            when(trasacaoInterface.findById(ID)).thenReturn(Optional.of(transacaoEntity));
            when(trasacaoInterface.save(any(ETransacao.class))).thenReturn(transacaoEntity);

            String resultado = sTrasacao.excluirTrasacaoPorId(ID);

            assertEquals("transação excluída com sucesso", resultado);
            assertEquals(EnStatus.I, transacaoEntity.getStatus());
            verify(trasacaoInterface, times(1)).save(transacaoEntity);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoTentarExcluirInexistente() {
            when(trasacaoInterface.findById(ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> sTrasacao.excluirTrasacaoPorId(ID));
            verify(trasacaoInterface, never()).save(any());
        }

        @Test
        void deveRetornarMensagemDeErro_QuandoOcorrerExcecaoNoBanco() {
            when(trasacaoInterface.findById(ID)).thenReturn(Optional.of(transacaoEntity));
            when(trasacaoInterface.save(any(ETransacao.class))).thenThrow(new RuntimeException("Erro de persistência"));

            String resultado = sTrasacao.excluirTrasacaoPorId(ID);

            assertEquals("erro ao excluir transação", resultado);
        }
    }
}