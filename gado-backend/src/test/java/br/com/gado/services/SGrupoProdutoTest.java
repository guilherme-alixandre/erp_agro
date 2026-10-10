package br.com.gado.services;

import br.com.gado.dto.grupoProdutoDto.GrupoProdutoCadastroDto;
import br.com.gado.dto.grupoProdutoDto.GrupoProdutoPutDto;
import br.com.gado.dto.grupoProdutoDto.GrupoProdutoRespostaDto;
import br.com.gado.entities.EGrupoProduto;
import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoInsumo;
import br.com.gado.repositories.IGrupoProduto;
import br.com.gado.repositories.IInsumo;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SGrupoProdutoTest {

    @InjectMocks
    private SGrupoProduto sGrupoProduto;

    @Mock
    private IGrupoProduto grupoProdutoInterface;

    @Mock
    private IInsumo insumoInterface;

    private EGrupoProduto grupo;

    @BeforeEach
    void setUp() {
        grupo = new EGrupoProduto();
        grupo.setId(1L);
        grupo.setNome("Rações");
        grupo.setCodigoPrefixo("01");
        grupo.setCategoriaGrupo(EnTipoInsumo.RACAO);
        grupo.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);
    }

    @Nested
    class ListarTests {

        @Test
        void deveListarTodos_QuandoSemFiltroDeStatusOuBusca() {
            when(grupoProdutoInterface.findAllByOrderByNomeAsc()).thenReturn(List.of(grupo));

            List<GrupoProdutoRespostaDto> resultado = sGrupoProduto.listar(null, null);

            assertThat(resultado).hasSize(1);
            verify(grupoProdutoInterface, never()).findByStatusOrderByNomeAsc(any());
        }

        @Test
        void deveFiltrarPorBusca_QuandoTermoInformado() {
            when(grupoProdutoInterface.findByNomeContainingIgnoreCaseOrderByNomeAsc("raç")).thenReturn(List.of(grupo));

            List<GrupoProdutoRespostaDto> resultado = sGrupoProduto.listar("raç", null);

            assertThat(resultado).hasSize(1);
        }

        @Test
        void deveFiltrarPorStatusAtivo_QuandoStatusAInformado() {
            when(grupoProdutoInterface.findByStatusOrderByNomeAsc(EnStatus.A)).thenReturn(List.of(grupo));

            sGrupoProduto.listar(null, "A");

            verify(grupoProdutoInterface).findByStatusOrderByNomeAsc(EnStatus.A);
        }

        @Test
        void deveFiltrarPorStatusEBusca_QuandoAmbosInformados() {
            when(grupoProdutoInterface.findByStatusAndNomeContainingIgnoreCaseOrderByNomeAsc(EnStatus.I, "raç"))
                    .thenReturn(List.of());

            sGrupoProduto.listar("raç", "I");

            verify(grupoProdutoInterface).findByStatusAndNomeContainingIgnoreCaseOrderByNomeAsc(EnStatus.I, "raç");
        }

        @Test
        void deveLancarExcecao_QuandoStatusInvalido() {
            assertThatThrownBy(() -> sGrupoProduto.listar(null, "X"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Status inválido");
        }
    }

    @Nested
    class BuscarPorIdTests {

        @Test
        void deveRetornarGrupo_QuandoEncontrado() {
            when(grupoProdutoInterface.findById(1L)).thenReturn(Optional.of(grupo));

            GrupoProdutoRespostaDto resultado = sGrupoProduto.buscarPorId(1L);

            assertThat(resultado.getNome()).isEqualTo("Rações");
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoNaoEncontrado() {
            when(grupoProdutoInterface.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sGrupoProduto.buscarPorId(99L)).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class CriarTests {

        private GrupoProdutoCadastroDto dtoValido() {
            GrupoProdutoCadastroDto dto = new GrupoProdutoCadastroDto();
            dto.setNome("Medicamentos");
            dto.setCodigoPrefixo("02");
            dto.setCategoriaGrupo(EnTipoInsumo.MEDICAMENTO);
            dto.setNaturezaFinanceira(EnNaturezaFinanceira.GASTO);
            return dto;
        }

        @Test
        void deveCriarGrupo_QuandoNomeEPrefixoUnicos() {
            when(grupoProdutoInterface.findFirstByNomeIgnoreCase("Medicamentos")).thenReturn(Optional.empty());
            when(grupoProdutoInterface.findFirstByCodigoPrefixo("02")).thenReturn(Optional.empty());
            when(grupoProdutoInterface.save(any(EGrupoProduto.class))).thenAnswer(inv -> inv.getArgument(0));

            GrupoProdutoRespostaDto resultado = sGrupoProduto.criar(dtoValido());

            assertThat(resultado.getNome()).isEqualTo("Medicamentos");
            assertThat(resultado.getCodigoPrefixo()).isEqualTo("02");
        }

        @Test
        void deveLancarExcecao_QuandoNomeJaExiste() {
            when(grupoProdutoInterface.findFirstByNomeIgnoreCase("Medicamentos")).thenReturn(Optional.of(grupo));

            assertThatThrownBy(() -> sGrupoProduto.criar(dtoValido()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Já existe um grupo de produto com esse nome");
        }

        @Test
        void deveLancarExcecao_QuandoPrefixoJaExiste() {
            when(grupoProdutoInterface.findFirstByNomeIgnoreCase("Medicamentos")).thenReturn(Optional.empty());
            when(grupoProdutoInterface.findFirstByCodigoPrefixo("02")).thenReturn(Optional.of(grupo));

            assertThatThrownBy(() -> sGrupoProduto.criar(dtoValido()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("prefixo");
        }
    }

    @Nested
    class AtualizarTests {

        @Test
        void deveAtualizarNome_QuandoNomeNaoConflita() {
            when(grupoProdutoInterface.findById(1L)).thenReturn(Optional.of(grupo));
            when(grupoProdutoInterface.findFirstByNomeIgnoreCase("Novo Nome")).thenReturn(Optional.empty());
            when(grupoProdutoInterface.save(any(EGrupoProduto.class))).thenAnswer(inv -> inv.getArgument(0));

            GrupoProdutoPutDto dto = new GrupoProdutoPutDto();
            dto.setNome("Novo Nome");

            GrupoProdutoRespostaDto resultado = sGrupoProduto.atualizar(1L, dto);

            assertThat(resultado.getNome()).isEqualTo("Novo Nome");
        }

        @Test
        void deveLancarExcecao_QuandoNovoNomeConflitaComOutroGrupo() {
            EGrupoProduto outroGrupo = new EGrupoProduto();
            outroGrupo.setId(2L);
            when(grupoProdutoInterface.findById(1L)).thenReturn(Optional.of(grupo));
            when(grupoProdutoInterface.findFirstByNomeIgnoreCase("Conflitante")).thenReturn(Optional.of(outroGrupo));

            GrupoProdutoPutDto dto = new GrupoProdutoPutDto();
            dto.setNome("Conflitante");

            assertThatThrownBy(() -> sGrupoProduto.atualizar(1L, dto))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void devePermitirManterMesmoNome_QuandoConflitoEhComElePropio() {
            when(grupoProdutoInterface.findById(1L)).thenReturn(Optional.of(grupo));
            when(grupoProdutoInterface.findFirstByNomeIgnoreCase("Rações")).thenReturn(Optional.of(grupo));
            when(grupoProdutoInterface.save(any(EGrupoProduto.class))).thenAnswer(inv -> inv.getArgument(0));

            GrupoProdutoPutDto dto = new GrupoProdutoPutDto();
            dto.setNome("Rações");

            GrupoProdutoRespostaDto resultado = sGrupoProduto.atualizar(1L, dto);

            assertThat(resultado.getNome()).isEqualTo("Rações");
        }

        @Test
        void deveLancarExcecao_QuandoAlteraPrefixoComProdutosVinculados() {
            when(grupoProdutoInterface.findById(1L)).thenReturn(Optional.of(grupo));
            when(grupoProdutoInterface.findFirstByCodigoPrefixo("09")).thenReturn(Optional.empty());
            when(insumoInterface.existsByGrupoProdutoId(1L)).thenReturn(true);

            GrupoProdutoPutDto dto = new GrupoProdutoPutDto();
            dto.setCodigoPrefixo("09");

            assertThatThrownBy(() -> sGrupoProduto.atualizar(1L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("produtos vinculados");
            verify(grupoProdutoInterface, never()).save(any());
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoGrupoNaoExiste() {
            when(grupoProdutoInterface.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sGrupoProduto.atualizar(99L, new GrupoProdutoPutDto()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class InativarReativarTests {

        @Test
        void deveInativarGrupo() {
            when(grupoProdutoInterface.findById(1L)).thenReturn(Optional.of(grupo));

            String resultado = sGrupoProduto.inativar(1L);

            assertThat(resultado).contains("inativado com sucesso");
            assertThat(grupo.getStatus()).isEqualTo(EnStatus.I);
        }

        @Test
        void deveReativarGrupo() {
            grupo.setStatus(EnStatus.I);
            when(grupoProdutoInterface.findById(1L)).thenReturn(Optional.of(grupo));

            String resultado = sGrupoProduto.reativar(1L);

            assertThat(resultado).contains("reativado com sucesso");
            assertThat(grupo.getStatus()).isEqualTo(EnStatus.A);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoInativarInexistente() {
            when(grupoProdutoInterface.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sGrupoProduto.inativar(99L)).isInstanceOf(EntityNotFoundException.class);
        }
    }
}
