package br.com.gado.services;

import br.com.gado.dto.CategoriaDTO;
import br.com.gado.entities.ECategoria;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.ICategoria;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SCategoriaTest {

    @InjectMocks
    private SCategoria sCategoria;

    @Mock
    private ICategoria categoriaInterface;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @Test
    void deveCriarCategoria() {
        when(categoriaInterface.save(any(ECategoria.class))).thenAnswer(inv -> {
            ECategoria c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CategoriaDTO dto = new CategoriaDTO();
        dto.setCategoria("Manutenção");

        CategoriaDTO resultado = sCategoria.criarCategoria(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getCategoria()).isEqualTo("Manutenção");
    }

    @Test
    void deveBuscarCategoriaAtivaPorId() {
        ECategoria categoria = new ECategoria();
        categoria.setId(1L);
        categoria.setCategoria("Manutenção");
        when(categoriaInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(categoria));

        CategoriaDTO resultado = sCategoria.buscarCategoriaPorId(1L);

        assertThat(resultado.getCategoria()).isEqualTo("Manutenção");
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoBuscarInativaOuInexistente() {
        when(categoriaInterface.findByIdAndStatus(99L, EnStatus.A)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sCategoria.buscarCategoriaPorId(99L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveAtualizarCategoria() {
        ECategoria categoria = new ECategoria();
        categoria.setId(1L);
        categoria.setCategoria("Nome antigo");
        when(categoriaInterface.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaInterface.save(any(ECategoria.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaDTO dto = new CategoriaDTO();
        dto.setCategoria("Nome novo");

        CategoriaDTO resultado = sCategoria.atualizarCategoriaPorId(dto, 1L);

        assertThat(resultado.getCategoria()).isEqualTo("Nome novo");
    }

    @Test
    void deveExcluirCategoria_InativandoStatus() {
        ECategoria categoria = new ECategoria();
        categoria.setId(1L);
        when(categoriaInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(categoria));
        when(categoriaInterface.save(any(ECategoria.class))).thenAnswer(inv -> inv.getArgument(0));

        String resultado = sCategoria.excluirCategoria(1L);

        assertThat(resultado).isEqualTo("Categoria excluida com sucesso");
        assertThat(categoria.getStatus()).isEqualTo(EnStatus.I);
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoExcluirInexistente() {
        when(categoriaInterface.findByIdAndStatus(99L, EnStatus.A)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sCategoria.excluirCategoria(99L)).isInstanceOf(EntityNotFoundException.class);
    }
}
