package br.com.gado.services;

import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueCadastroDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueCancelamentoDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueEdicaoDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueItemCadastroDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueRespostaDto;
import br.com.gado.entities.EConsumoEstoque;
import br.com.gado.entities.EConsumoEstoqueItem;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoInsumo;
import br.com.gado.repositories.IConsumoEstoque;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
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
class SConsumoEstoqueTest {

    @InjectMocks
    private SConsumoEstoque sConsumoEstoque;

    @Mock private IConsumoEstoque consumoEstoqueInterface;
    @Mock private IInsumo insumoInterface;
    @Mock private IUnidadeMedida unidadeMedidaInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private SLancamentoFinanceiro lancamentoFinanceiroService;
    @Mock private SInsumo insumoService;

    private EUnidadeMedida primaria;
    private EInsumo insumo;
    private EUsuario autor;

    @BeforeEach
    void setUp() {
        primaria = new EUnidadeMedida();
        primaria.setId(1L);
        primaria.setUnidade("KG");

        insumo = new EInsumo();
        insumo.setId(200L);
        insumo.setNome("Sal Mineral");
        insumo.setTipo(EnTipoInsumo.OUTROS);
        insumo.setSaldoAtual(100.0);
        insumo.setUnidadeMedidaPrimaria(primaria);
        insumo.setPrecoCompraMedio(3.0);

        autor = new EUsuario();
        autor.setEmail("cuidador@fazenda.com");
        autor.setPerfil(EnPerfilUsuario.CUIDADOR);
    }

    private void stubUsuario(EUsuario usuario) {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(usuario));
    }

    private ConsumoEstoqueCadastroDto dtoCadastro(double quantidade) {
        ConsumoEstoqueItemCadastroDto item = new ConsumoEstoqueItemCadastroDto();
        item.setInsumoId(insumo.getId());
        item.setQuantidade(quantidade);

        ConsumoEstoqueCadastroDto dto = new ConsumoEstoqueCadastroDto();
        dto.setMotivo("Suplementação mineral do lote");
        dto.setItens(List.of(item));
        return dto;
    }

    @Nested
    class RegistrarConsumoTests {

        @Test
        void deveRegistrarConsumo_DebitandoEstoque() {
            stubUsuario(autor);
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));
            when(consumoEstoqueInterface.save(any(EConsumoEstoque.class))).thenAnswer(inv -> inv.getArgument(0));

            ConsumoEstoqueRespostaDto resultado = sConsumoEstoque.registrarConsumo(dtoCadastro(10.0), autor.getEmail());

            assertThat(insumo.getSaldoAtual()).isEqualTo(90.0);
            assertThat(resultado.getItens()).hasSize(1);
            assertThat(resultado.getItens().get(0).getQuantidadeBaixaUnidadePrimaria()).isEqualTo(10.0);
            verify(lancamentoFinanceiroService).contabilizarConsumoEstoque(any());
        }

        @Test
        void deveLancarExcecao_QuandoInsumoEhCabecaDeAnimal() {
            stubUsuario(autor);
            insumo.setTipo(EnTipoInsumo.ANIMAL);
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));

            assertThatThrownBy(() -> sConsumoEstoque.registrarConsumo(dtoCadastro(10.0), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("venda no Financeiro");
        }

        @Test
        void deveLancarExcecao_QuandoInsumoEhVacina() {
            stubUsuario(autor);
            insumo.setTipo(EnTipoInsumo.VACINA);
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));

            assertThatThrownBy(() -> sConsumoEstoque.registrarConsumo(dtoCadastro(10.0), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Vacinar Animais");
        }

        @Test
        void deveLancarExcecao_QuandoEstoqueInsuficiente() {
            stubUsuario(autor);
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));

            assertThatThrownBy(() -> sConsumoEstoque.registrarConsumo(dtoCadastro(1000.0), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Estoque insuficiente");
        }

        @Test
        void deveLancarExcecao_QuandoProdutoNaoEncontrado() {
            stubUsuario(autor);
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sConsumoEstoque.registrarConsumo(dtoCadastro(10.0), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class CancelarConsumoTests {

        private EConsumoEstoque consumoComItem() {
            EConsumoEstoqueItem item = new EConsumoEstoqueItem();
            item.setInsumo(insumo);
            item.setUnidadeRegistro(primaria);
            item.setQuantidadeBaixaUnidadePrimaria(10.0);

            EConsumoEstoque consumo = new EConsumoEstoque();
            consumo.setId(900L);
            consumo.setCancelado(false);
            consumo.setCriadoPorEmail(autor.getEmail());
            consumo.setItens(List.of(item));
            return consumo;
        }

        @Test
        void deveCancelar_EstornandoEstoque() {
            stubUsuario(autor);
            insumo.setSaldoAtual(90.0);
            EConsumoEstoque consumo = consumoComItem();
            when(consumoEstoqueInterface.findById(900L)).thenReturn(Optional.of(consumo));
            when(consumoEstoqueInterface.save(any(EConsumoEstoque.class))).thenAnswer(inv -> inv.getArgument(0));

            ConsumoEstoqueCancelamentoDto dto = new ConsumoEstoqueCancelamentoDto();
            dto.setMotivoCancelamento("Registrado por engano");

            sConsumoEstoque.cancelarConsumo(900L, dto, autor.getEmail());

            assertThat(insumo.getSaldoAtual()).isEqualTo(100.0);
            assertThat(consumo.getCancelado()).isTrue();
            verify(lancamentoFinanceiroService).estornarSaidaConsumoEstoque(consumo);
        }

        @Test
        void deveLancarExcecao_QuandoJaCancelado() {
            stubUsuario(autor);
            EConsumoEstoque consumo = consumoComItem();
            consumo.setCancelado(true);
            when(consumoEstoqueInterface.findById(900L)).thenReturn(Optional.of(consumo));

            assertThatThrownBy(() -> sConsumoEstoque.cancelarConsumo(
                    900L, new ConsumoEstoqueCancelamentoDto(), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("já foi cancelada");
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioNaoEhAutorNemAdministrador() {
            EUsuario outroUsuario = new EUsuario();
            outroUsuario.setEmail("outro@fazenda.com");
            outroUsuario.setPerfil(EnPerfilUsuario.CUIDADOR);
            stubUsuario(outroUsuario);
            EConsumoEstoque consumo = consumoComItem();
            when(consumoEstoqueInterface.findById(900L)).thenReturn(Optional.of(consumo));

            assertThatThrownBy(() -> sConsumoEstoque.cancelarConsumo(
                    900L, new ConsumoEstoqueCancelamentoDto(), outroUsuario.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("só pode cancelar");
        }
    }

    @Nested
    class EditarConsumoTests {

        @Test
        void deveEditar_EstornandoBaixaAntigaEAplicandoNova() {
            stubUsuario(autor);
            EInsumo insumoAntigo = new EInsumo();
            insumoAntigo.setId(300L);
            insumoAntigo.setSaldoAtual(50.0);
            insumoAntigo.setUnidadeMedidaPrimaria(primaria);

            EConsumoEstoqueItem itemAntigo = new EConsumoEstoqueItem();
            itemAntigo.setInsumo(insumoAntigo);
            itemAntigo.setQuantidadeBaixaUnidadePrimaria(5.0);

            EConsumoEstoque consumo = new EConsumoEstoque();
            consumo.setId(900L);
            consumo.setCancelado(false);
            consumo.setCriadoPorEmail(autor.getEmail());
            consumo.setItens(new java.util.ArrayList<>(List.of(itemAntigo)));

            when(consumoEstoqueInterface.findById(900L)).thenReturn(Optional.of(consumo));
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));
            when(consumoEstoqueInterface.save(any(EConsumoEstoque.class))).thenAnswer(inv -> inv.getArgument(0));

            ConsumoEstoqueEdicaoDto dto = new ConsumoEstoqueEdicaoDto();
            dto.setMotivo("Motivo atualizado");
            ConsumoEstoqueItemCadastroDto novoItem = new ConsumoEstoqueItemCadastroDto();
            novoItem.setInsumoId(insumo.getId());
            novoItem.setQuantidade(20.0);
            dto.setItens(List.of(novoItem));

            ConsumoEstoqueRespostaDto resultado = sConsumoEstoque.editarConsumo(900L, dto, autor.getEmail());

            assertThat(insumoAntigo.getSaldoAtual()).isEqualTo(55.0);
            assertThat(insumo.getSaldoAtual()).isEqualTo(80.0);
            assertThat(resultado.getMotivo()).isEqualTo("Motivo atualizado");
            verify(lancamentoFinanceiroService).estornarSaidaConsumoEstoque(consumo);
            verify(lancamentoFinanceiroService).contabilizarConsumoEstoque(consumo);
        }

        @Test
        void deveLancarExcecao_QuandoConsumoJaCancelado() {
            stubUsuario(autor);
            EConsumoEstoque consumo = new EConsumoEstoque();
            consumo.setId(900L);
            consumo.setCancelado(true);
            consumo.setCriadoPorEmail(autor.getEmail());
            when(consumoEstoqueInterface.findById(900L)).thenReturn(Optional.of(consumo));

            assertThatThrownBy(() -> sConsumoEstoque.editarConsumo(
                    900L, new ConsumoEstoqueEdicaoDto(), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("já cancelada");
        }
    }

    @Nested
    class ListarTodosTests {

        @Test
        void deveListarSemFiltroDeData() {
            when(consumoEstoqueInterface.findAllByOrderByDataConsumoDesc()).thenReturn(List.of());

            assertThat(sConsumoEstoque.listarTodos(null, null)).isEmpty();
        }
    }
}
