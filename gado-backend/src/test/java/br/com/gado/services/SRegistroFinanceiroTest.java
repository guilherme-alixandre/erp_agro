package br.com.gado.services;

import br.com.gado.dto.RegistroFinanceiroDTO;
import br.com.gado.entities.ECategoria;
import br.com.gado.entities.ERegistroFinanceiro;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.ICategoria;
import br.com.gado.repositories.IRegistroFinanceiro;
import br.com.gado.repositories.IUsuario;
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
class SRegistroFinanceiroTest {

    @Mock private IRegistroFinanceiro registroFinanceiroInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private ICategoria categoriaInterface;

    private SRegistroFinanceiro sRegistroFinanceiro;

    private EUsuario usuario;
    private ECategoria categoria;

    @BeforeEach
    void setUp() {
        sRegistroFinanceiro = new SRegistroFinanceiro(
                registroFinanceiroInterface, usuarioInterface, categoriaInterface, new ModelMapper());

        usuario = new EUsuario();
        usuario.setId(1L);
        usuario.setEmail("financeiro@fazenda.com");
        usuario.setStatus(EnStatus.A);

        categoria = new ECategoria();
        categoria.setId(1L);
        categoria.setCategoria("Manutenção");
    }

    private RegistroFinanceiroDTO dtoValido() {
        RegistroFinanceiroDTO dto = new RegistroFinanceiroDTO();
        dto.setDescricao("Conserto de trator");
        dto.setValor(500.0);
        dto.setUsuarioId(usuario);
        dto.setCategoriaId(categoria);
        return dto;
    }

    @Test
    void deveCriarRegistroFinanceiro() {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A)).thenReturn(Optional.of(usuario));
        when(categoriaInterface.findById(1L)).thenReturn(Optional.of(categoria));
        when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class))).thenAnswer(inv -> {
            ERegistroFinanceiro e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });

        RegistroFinanceiroDTO resultado = sRegistroFinanceiro.criarRegistroFinanceiro(dtoValido());

        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getDescricao()).isEqualTo("Conserto de trator");
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoUsuarioNaoEncontrado() {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sRegistroFinanceiro.criarRegistroFinanceiro(dtoValido()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoCategoriaNaoEncontrada() {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A)).thenReturn(Optional.of(usuario));
        when(categoriaInterface.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sRegistroFinanceiro.criarRegistroFinanceiro(dtoValido()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveBuscarPorId() {
        ERegistroFinanceiro registro = new ERegistroFinanceiro();
        registro.setId(10L);
        registro.setDescricao("Conserto de trator");
        when(registroFinanceiroInterface.findById(10L)).thenReturn(Optional.of(registro));

        RegistroFinanceiroDTO resultado = sRegistroFinanceiro.buscarRegistroFinanceiroPorId(10L);

        assertThat(resultado.getDescricao()).isEqualTo("Conserto de trator");
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoBuscarInexistente() {
        when(registroFinanceiroInterface.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sRegistroFinanceiro.buscarRegistroFinanceiroPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveAtualizarRegistro() {
        ERegistroFinanceiro registro = new ERegistroFinanceiro();
        registro.setId(10L);
        registro.setDescricao("Descrição antiga");
        when(registroFinanceiroInterface.findById(10L)).thenReturn(Optional.of(registro));
        when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistroFinanceiroDTO dto = new RegistroFinanceiroDTO();
        dto.setDescricao("Descrição nova");

        RegistroFinanceiroDTO resultado = sRegistroFinanceiro.atualizarRegistroFinanceiroPorId(10L, dto);

        assertThat(resultado.getDescricao()).isEqualTo("Descrição nova");
    }

    @Test
    void deveExcluirRegistro_InativandoStatus() {
        ERegistroFinanceiro registro = new ERegistroFinanceiro();
        registro.setId(10L);
        when(registroFinanceiroInterface.findById(10L)).thenReturn(Optional.of(registro));
        when(registroFinanceiroInterface.save(any(ERegistroFinanceiro.class))).thenAnswer(inv -> inv.getArgument(0));

        String resultado = sRegistroFinanceiro.excluirRegistroFinanceiroPorId(10L);

        assertThat(resultado).isEqualTo("registro financeiro excluído com sucesso");
        assertThat(registro.getStatus()).isEqualTo(EnStatus.I);
    }

    @Test
    void deveLancarEntityNotFoundException_QuandoExcluirInexistente() {
        when(registroFinanceiroInterface.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sRegistroFinanceiro.excluirRegistroFinanceiroPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
