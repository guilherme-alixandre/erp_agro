package br.com.gado.services;

import br.com.gado.dto.MovimentacaoEstoqueDTO;
import br.com.gado.dto.movimentacaoEstoqueDto.MovimentacaoEstoqueRespostaDto;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.EMovimentacaoEstoque;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoMovimentacaoEstoque;
import br.com.gado.repositories.IMovimentacaoEstoque;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SMovimentacaoEstoqueTest {

    @Mock
    private IMovimentacaoEstoque movimentacaoEstoqueInterface;

    private SMovimentacaoEstoque sMovimentacaoEstoque;

    @BeforeEach
    void setUp() {
        sMovimentacaoEstoque = new SMovimentacaoEstoque(movimentacaoEstoqueInterface, new ModelMapper());
    }

    @Test
    void deveListarTodasAsMovimentacoes_QuandoSemFiltroDeInsumo() {
        EMovimentacaoEstoque mov = new EMovimentacaoEstoque();
        mov.setId(1L);
        mov.setEnTipoMovimentacaoEstoque(EnTipoMovimentacaoEstoque.ENTRADA);
        mov.setQuantidade(10.0);
        when(movimentacaoEstoqueInterface.findAllByOrderByDataMovimentacaoDesc()).thenReturn(List.of(mov));

        List<MovimentacaoEstoqueRespostaDto> resultado = sMovimentacaoEstoque.listarTodas(null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipo()).isEqualTo(EnTipoMovimentacaoEstoque.ENTRADA);
    }

    @Test
    void deveFiltrarPorInsumo_QuandoIdInformado() {
        EInsumo insumo = new EInsumo();
        insumo.setId(50L);
        insumo.setNome("Ração");
        EMovimentacaoEstoque mov = new EMovimentacaoEstoque();
        mov.setId(1L);
        mov.setInsumoId(insumo);
        when(movimentacaoEstoqueInterface.findByInsumoId_IdOrderByDataMovimentacaoDesc(50L)).thenReturn(List.of(mov));

        List<MovimentacaoEstoqueRespostaDto> resultado = sMovimentacaoEstoque.listarTodas(50L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getInsumoNome()).isEqualTo("Ração");
    }

    @Test
    void deveCriarMovimentacaoEstoque() {
        when(movimentacaoEstoqueInterface.save(any(EMovimentacaoEstoque.class))).thenAnswer(inv -> {
            EMovimentacaoEstoque m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        MovimentacaoEstoqueDTO dto = new MovimentacaoEstoqueDTO();
        dto.setQuantidade(5.0);

        MovimentacaoEstoqueDTO resultado = sMovimentacaoEstoque.criarMovimentacaoEstoque(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getQuantidade()).isEqualTo(5.0);
    }

    @Test
    void deveBuscarMovimentacaoPorId() {
        EMovimentacaoEstoque mov = new EMovimentacaoEstoque();
        mov.setId(1L);
        mov.setQuantidade(10.0);
        when(movimentacaoEstoqueInterface.findById(1L)).thenReturn(Optional.of(mov));

        MovimentacaoEstoqueDTO resultado = sMovimentacaoEstoque.buscarMovimentacaoEstoquePorId(1L);

        assertThat(resultado.getQuantidade()).isEqualTo(10.0);
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoBuscarMovimentacaoInexistente() {
        when(movimentacaoEstoqueInterface.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sMovimentacaoEstoque.buscarMovimentacaoEstoquePorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveExcluirMovimentacao_InativandoStatus() {
        EMovimentacaoEstoque mov = new EMovimentacaoEstoque();
        mov.setId(1L);
        when(movimentacaoEstoqueInterface.findById(1L)).thenReturn(Optional.of(mov));
        when(movimentacaoEstoqueInterface.save(any(EMovimentacaoEstoque.class))).thenAnswer(inv -> inv.getArgument(0));

        String resultado = sMovimentacaoEstoque.excluirMovimentacaoEstoquePorId(1L);

        assertThat(resultado).isEqualTo("movimentação de estoque excluída com sucesso");
        assertThat(mov.getStatus()).isEqualTo(EnStatus.I);
    }
}
