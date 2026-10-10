package br.com.gado.services;

import br.com.gado.dto.TrasacaoDTO;
import br.com.gado.entities.ELote;
import br.com.gado.entities.EParceiro;
import br.com.gado.entities.ETransacao;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.IParceiro;
import br.com.gado.repositories.ITrasacao;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class STrasacaoTest {

    @Mock private ITrasacao trasacaoInterface;
    @Mock private IParceiro parceiroInterface;
    @Mock private ILote loteInterface;

    private STrasacao sTrasacao;

    private EParceiro parceiro;
    private ELote lote;

    @BeforeEach
    void setUp() {
        sTrasacao = new STrasacao(trasacaoInterface, parceiroInterface, loteInterface, new ModelMapper());

        parceiro = new EParceiro();
        parceiro.setId(1L);
        parceiro.setCpfCnpj("12345678909");

        lote = new ELote();
        lote.setId(1L);
        lote.setCodigo("LOT001");
    }

    private TrasacaoDTO dtoValido() {
        TrasacaoDTO dto = new TrasacaoDTO();
        dto.setValor(1000.0);
        dto.setParceiro(parceiro);
        dto.setLote(lote);
        return dto;
    }

    @Test
    void deveCriarTransacao() {
        when(parceiroInterface.findByCpfCnpj("12345678909")).thenReturn(Optional.of(parceiro));
        when(loteInterface.findById(1L)).thenReturn(Optional.of(lote));
        when(trasacaoInterface.save(any(ETransacao.class))).thenAnswer(inv -> {
            ETransacao t = inv.getArgument(0);
            t.setId(5L);
            return t;
        });

        TrasacaoDTO resultado = sTrasacao.criarTrasacao(dtoValido());

        assertThat(resultado.getId()).isEqualTo(5L);
        assertThat(resultado.getValor()).isEqualTo(1000.0);
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoParceiroNaoEncontrado() {
        when(parceiroInterface.findByCpfCnpj("12345678909")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sTrasacao.criarTrasacao(dtoValido()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoLoteNaoEncontrado() {
        when(parceiroInterface.findByCpfCnpj("12345678909")).thenReturn(Optional.of(parceiro));
        when(loteInterface.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sTrasacao.criarTrasacao(dtoValido()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveBuscarPorId() {
        ETransacao transacao = new ETransacao();
        transacao.setId(5L);
        transacao.setValor(1000.0);
        when(trasacaoInterface.findById(5L)).thenReturn(Optional.of(transacao));

        TrasacaoDTO resultado = sTrasacao.buscarTrasacaoPorId(5L);

        assertThat(resultado.getValor()).isEqualTo(1000.0);
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoBuscarInexistente() {
        when(trasacaoInterface.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sTrasacao.buscarTrasacaoPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveAtualizarTransacao() {
        ETransacao transacao = new ETransacao();
        transacao.setId(5L);
        transacao.setValor(1000.0);
        when(trasacaoInterface.findById(5L)).thenReturn(Optional.of(transacao));
        when(trasacaoInterface.save(any(ETransacao.class))).thenAnswer(inv -> inv.getArgument(0));

        TrasacaoDTO dto = new TrasacaoDTO();
        dto.setValor(2000.0);

        TrasacaoDTO resultado = sTrasacao.atualizarTrasacaoPorId(5L, dto);

        assertThat(resultado.getValor()).isEqualTo(2000.0);
    }

    @Test
    void deveExcluirTransacao_InativandoStatus() {
        ETransacao transacao = new ETransacao();
        transacao.setId(5L);
        when(trasacaoInterface.findById(5L)).thenReturn(Optional.of(transacao));
        when(trasacaoInterface.save(any(ETransacao.class))).thenAnswer(inv -> inv.getArgument(0));

        String resultado = sTrasacao.excluirTrasacaoPorId(5L);

        assertThat(resultado).isEqualTo("transação excluída com sucesso");
        assertThat(transacao.getStatus()).isEqualTo(EnStatus.I);
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoExcluirInexistente() {
        when(trasacaoInterface.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sTrasacao.excluirTrasacaoPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
