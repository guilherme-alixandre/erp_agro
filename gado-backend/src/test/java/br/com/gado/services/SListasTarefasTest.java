package br.com.gado.services;

import br.com.gado.dto.ListasTarefasDTO;
import br.com.gado.entities.EListasTarefas;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IListasTarefas;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SListasTarefasTest {

    @Mock
    private IListasTarefas listasTarefasInterface;

    private SListasTarefas sListasTarefas;

    @BeforeEach
    void setUp() {
        sListasTarefas = new SListasTarefas(listasTarefasInterface, new ModelMapper());
    }

    @Test
    void deveCriarListaDeTarefas() {
        when(listasTarefasInterface.save(any(EListasTarefas.class))).thenAnswer(inv -> {
            EListasTarefas lista = inv.getArgument(0);
            lista.setId(1L);
            return lista;
        });

        ListasTarefasDTO dto = new ListasTarefasDTO();
        dto.setNomeLista("Tarefas de João");

        ListasTarefasDTO resultado = sListasTarefas.criarListaDeTarefas(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNomeLista()).isEqualTo("Tarefas de João");
    }

    @Test
    void deveBuscarListaPorId() {
        EListasTarefas lista = new EListasTarefas();
        lista.setId(1L);
        lista.setNomeLista("Tarefas de João");
        when(listasTarefasInterface.findById(1L)).thenReturn(Optional.of(lista));

        ListasTarefasDTO resultado = sListasTarefas.buscarListaDeTarefasPorId(1L);

        assertThat(resultado.getNomeLista()).isEqualTo("Tarefas de João");
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoListaNaoExiste() {
        when(listasTarefasInterface.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sListasTarefas.buscarListaDeTarefasPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveAtualizarListaDeTarefas() {
        EListasTarefas lista = new EListasTarefas();
        lista.setId(1L);
        lista.setNomeLista("Nome antigo");
        when(listasTarefasInterface.findById(1L)).thenReturn(Optional.of(lista));
        when(listasTarefasInterface.save(any(EListasTarefas.class))).thenAnswer(inv -> inv.getArgument(0));

        ListasTarefasDTO dto = new ListasTarefasDTO();
        dto.setNomeLista("Nome novo");

        ListasTarefasDTO resultado = sListasTarefas.atualizarListaDeTarefasPorId(dto, 1L);

        assertThat(resultado.getNomeLista()).isEqualTo("Nome novo");
    }

    @Test
    void deveExcluirListaDeTarefas_InativandoStatus() {
        EListasTarefas lista = new EListasTarefas();
        lista.setId(1L);
        when(listasTarefasInterface.findById(1L)).thenReturn(Optional.of(lista));
        when(listasTarefasInterface.save(any(EListasTarefas.class))).thenAnswer(inv -> inv.getArgument(0));

        String resultado = sListasTarefas.excluirListaDeTarefas(1L);

        assertThat(resultado).isEqualTo("lista de tarefas excluída com sucesso");
        assertThat(lista.getStatus()).isEqualTo(EnStatus.I);
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoExcluirListaInexistente() {
        when(listasTarefasInterface.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sListasTarefas.excluirListaDeTarefas(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
