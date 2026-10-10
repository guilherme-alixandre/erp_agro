package br.com.gado.services;

import br.com.gado.dto.unidadeMedidaDto.UnidadeMedidaCadastroDto;
import br.com.gado.dto.unidadeMedidaDto.UnidadeMedidaPutDto;
import br.com.gado.dto.unidadeMedidaDto.UnidadeMedidaRespostaDto;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IUnidadeMedida;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SUnidadeMedidaTest {

    @InjectMocks
    private SUnidadeMedida sUnidadeMedida;

    @Mock
    private IUnidadeMedida unidadeMedidaInterface;

    private EUnidadeMedida saca;

    @BeforeEach
    void setUp() {
        saca = new EUnidadeMedida();
        saca.setId(1L);
        saca.setUnidade("SACA");
    }

    @Nested
    class ListarTests {

        @Test
        void deveLancarExcecao_QuandoStatusInvalido() {
            assertThatThrownBy(() -> sUnidadeMedida.listar(null, "X"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void deveListarTodas_SemFiltro() {
            when(unidadeMedidaInterface.findAllByOrderByUnidadeAsc()).thenReturn(java.util.List.of(saca));

            assertThat(sUnidadeMedida.listar(null, null)).hasSize(1);
        }
    }

    @Nested
    class CriarTests {

        @Test
        void deveCriarUnidadeMedida_EmMaiusculas() {
            when(unidadeMedidaInterface.findFirstByUnidadeIgnoreCase("KG")).thenReturn(Optional.empty());
            when(unidadeMedidaInterface.save(any(EUnidadeMedida.class))).thenAnswer(inv -> inv.getArgument(0));

            UnidadeMedidaCadastroDto dto = new UnidadeMedidaCadastroDto();
            dto.setUnidade("kg");

            UnidadeMedidaRespostaDto resultado = sUnidadeMedida.criar(dto);

            assertThat(resultado.getUnidade()).isEqualTo("KG");
        }

        @Test
        void deveLancarExcecao_QuandoJaExiste() {
            when(unidadeMedidaInterface.findFirstByUnidadeIgnoreCase("SACA")).thenReturn(Optional.of(saca));

            UnidadeMedidaCadastroDto dto = new UnidadeMedidaCadastroDto();
            dto.setUnidade("saca");

            assertThatThrownBy(() -> sUnidadeMedida.criar(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Já existe uma unidade de medida");
        }
    }

    @Nested
    class AtualizarTests {

        @Test
        void deveAtualizarUnidade() {
            when(unidadeMedidaInterface.findById(1L)).thenReturn(Optional.of(saca));
            when(unidadeMedidaInterface.findFirstByUnidadeIgnoreCase("TONELADA")).thenReturn(Optional.empty());
            when(unidadeMedidaInterface.save(any(EUnidadeMedida.class))).thenAnswer(inv -> inv.getArgument(0));

            UnidadeMedidaPutDto dto = new UnidadeMedidaPutDto();
            dto.setUnidade("tonelada");

            UnidadeMedidaRespostaDto resultado = sUnidadeMedida.atualizar(1L, dto);

            assertThat(resultado.getUnidade()).isEqualTo("TONELADA");
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoNaoExiste() {
            when(unidadeMedidaInterface.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sUnidadeMedida.atualizar(99L, new UnidadeMedidaPutDto()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class InativarReativarTests {

        @Test
        void deveInativarUnidade() {
            when(unidadeMedidaInterface.findById(1L)).thenReturn(Optional.of(saca));

            String resultado = sUnidadeMedida.inativar(1L);

            assertThat(resultado).contains("inativada com sucesso");
            assertThat(saca.getStatus()).isEqualTo(EnStatus.I);
        }

        @Test
        void deveReativarUnidade() {
            saca.setStatus(EnStatus.I);
            when(unidadeMedidaInterface.findById(1L)).thenReturn(Optional.of(saca));

            String resultado = sUnidadeMedida.reativar(1L);

            assertThat(resultado).contains("reativada com sucesso");
            assertThat(saca.getStatus()).isEqualTo(EnStatus.A);
        }
    }
}
