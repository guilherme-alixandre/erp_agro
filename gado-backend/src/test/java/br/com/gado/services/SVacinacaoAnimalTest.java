package br.com.gado.services;

import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalCadastroDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalCancelamentoDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalEdicaoDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalRespostaDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.entities.EVacinacaoAnimal;
import br.com.gado.entities.EVacinacaoAnimalItem;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoInsumo;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import br.com.gado.repositories.IVacinacaoAnimal;
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
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SVacinacaoAnimalTest {

    @InjectMocks
    private SVacinacaoAnimal sVacinacaoAnimal;

    @Mock private IVacinacaoAnimal vacinacaoAnimalInterface;
    @Mock private IInsumo insumoInterface;
    @Mock private IAnimal animalInterface;
    @Mock private ILote loteInterface;
    @Mock private IUnidadeMedida unidadeMedidaInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private SInsumo insumoService;
    @Mock private SCustoAnimal custoAnimalService;

    private EUnidadeMedida primaria;
    private EInsumo vacina;
    private EUsuario autor;

    @BeforeEach
    void setUp() {
        primaria = new EUnidadeMedida();
        primaria.setId(1L);
        primaria.setUnidade("ML");

        vacina = new EInsumo();
        vacina.setId(200L);
        vacina.setNome("Vacina Aftosa");
        vacina.setTipo(EnTipoInsumo.VACINA);
        vacina.setSaldoAtual(1000.0);
        vacina.setUnidadeMedidaPrimaria(primaria);
        vacina.setPrecoCompraMedio(2.0);

        autor = new EUsuario();
        autor.setEmail("cuidador@fazenda.com");
        autor.setPerfil(EnPerfilUsuario.CUIDADOR);
    }

    private void stubUsuario(EUsuario usuario) {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(usuario));
    }

    private EAnimal animalAtivo(Long id) {
        EAnimal animal = new EAnimal();
        animal.setId(id);
        animal.setCodigoBrinco("BR-" + id);
        animal.setStatus(EnStatus.A);
        return animal;
    }

    private VacinacaoAnimalCadastroDto dtoCadastro(double dosePorAnimal, Long... animalIds) {
        VacinacaoAnimalCadastroDto dto = new VacinacaoAnimalCadastroDto();
        dto.setInsumoId(vacina.getId());
        dto.setQuantidadePorAnimal(dosePorAnimal);
        dto.setAnimalIds(List.of(animalIds));
        return dto;
    }

    @Nested
    class RegistrarAplicacaoTests {

        @Test
        void deveRegistrarAplicacao_DebitandoEstoqueParaTodosOsAnimais() {
            stubUsuario(autor);
            when(insumoInterface.findByIdAndStatus(vacina.getId(), EnStatus.A)).thenReturn(Optional.of(vacina));
            EAnimal a1 = animalAtivo(1L);
            EAnimal a2 = animalAtivo(2L);
            when(animalInterface.findAllById(List.of(1L, 2L))).thenReturn(List.of(a1, a2));
            when(vacinacaoAnimalInterface.save(any(EVacinacaoAnimal.class))).thenAnswer(inv -> inv.getArgument(0));

            VacinacaoAnimalRespostaDto resultado =
                    sVacinacaoAnimal.registrarAplicacao(dtoCadastro(5.0, 1L, 2L), autor.getEmail());

            assertThat(vacina.getSaldoAtual()).isEqualTo(990.0);
            assertThat(resultado.getTotalAnimais()).isEqualTo(2);
            assertThat(resultado.getQuantidadeTotalBaixaUnidadePrimaria()).isEqualTo(10.0);
            verify(insumoService, times(2)).registrarMovimentacao(any(), any(), anyDouble(), any(), any(), any(), any(), any());
        }

        @Test
        void deveLancarExcecao_QuandoProdutoNaoEhVacina() {
            stubUsuario(autor);
            vacina.setTipo(EnTipoInsumo.RACAO);
            when(insumoInterface.findByIdAndStatus(vacina.getId(), EnStatus.A)).thenReturn(Optional.of(vacina));

            assertThatThrownBy(() -> sVacinacaoAnimal.registrarAplicacao(dtoCadastro(5.0, 1L), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não é uma vacina");
        }

        @Test
        void deveLancarExcecao_QuandoAnimalNaoEncontradoOuInativo() {
            stubUsuario(autor);
            when(insumoInterface.findByIdAndStatus(vacina.getId(), EnStatus.A)).thenReturn(Optional.of(vacina));
            when(animalInterface.findAllById(List.of(1L))).thenReturn(List.of());

            assertThatThrownBy(() -> sVacinacaoAnimal.registrarAplicacao(dtoCadastro(5.0, 1L), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não foram encontrados ou estão inativos");
        }

        @Test
        void deveLancarExcecao_QuandoEstoqueInsuficiente() {
            stubUsuario(autor);
            when(insumoInterface.findByIdAndStatus(vacina.getId(), EnStatus.A)).thenReturn(Optional.of(vacina));
            when(animalInterface.findAllById(List.of(1L))).thenReturn(List.of(animalAtivo(1L)));

            assertThatThrownBy(() -> sVacinacaoAnimal.registrarAplicacao(dtoCadastro(100000.0, 1L), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Estoque insuficiente");
        }
    }

    @Nested
    class CancelarAplicacaoTests {

        private EVacinacaoAnimal vacinacaoExistente() {
            EVacinacaoAnimalItem item = new EVacinacaoAnimalItem();
            item.setAnimal(animalAtivo(1L));

            EVacinacaoAnimal vacinacao = new EVacinacaoAnimal();
            vacinacao.setId(900L);
            vacinacao.setInsumo(vacina);
            vacinacao.setUnidadeRegistro(primaria);
            vacinacao.setQuantidadeBaixaPorAnimalUnidadePrimaria(5.0);
            vacinacao.setQuantidadeTotalBaixaUnidadePrimaria(5.0);
            vacinacao.setCriadoPorEmail(autor.getEmail());
            vacinacao.setCancelado(false);
            vacinacao.setItens(List.of(item));
            return vacinacao;
        }

        @Test
        void deveCancelar_EstornandoEstoque() {
            stubUsuario(autor);
            vacina.setSaldoAtual(995.0);
            EVacinacaoAnimal vacinacao = vacinacaoExistente();
            when(vacinacaoAnimalInterface.findById(900L)).thenReturn(Optional.of(vacinacao));
            when(vacinacaoAnimalInterface.save(any(EVacinacaoAnimal.class))).thenAnswer(inv -> inv.getArgument(0));

            VacinacaoAnimalCancelamentoDto dto = new VacinacaoAnimalCancelamentoDto();
            dto.setMotivoCancelamento("Registrado por engano");

            sVacinacaoAnimal.cancelarAplicacao(900L, dto, autor.getEmail());

            assertThat(vacina.getSaldoAtual()).isEqualTo(1000.0);
            assertThat(vacinacao.getCancelado()).isTrue();
        }

        @Test
        void deveLancarExcecao_QuandoJaCancelada() {
            stubUsuario(autor);
            EVacinacaoAnimal vacinacao = vacinacaoExistente();
            vacinacao.setCancelado(true);
            when(vacinacaoAnimalInterface.findById(900L)).thenReturn(Optional.of(vacinacao));

            assertThatThrownBy(() -> sVacinacaoAnimal.cancelarAplicacao(
                    900L, new VacinacaoAnimalCancelamentoDto(), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("já foi cancelada");
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioSemPermissao() {
            EUsuario outroUsuario = new EUsuario();
            outroUsuario.setEmail("outro@fazenda.com");
            outroUsuario.setPerfil(EnPerfilUsuario.CUIDADOR);
            stubUsuario(outroUsuario);
            EVacinacaoAnimal vacinacao = vacinacaoExistente();
            when(vacinacaoAnimalInterface.findById(900L)).thenReturn(Optional.of(vacinacao));

            assertThatThrownBy(() -> sVacinacaoAnimal.cancelarAplicacao(
                    900L, new VacinacaoAnimalCancelamentoDto(), outroUsuario.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("só pode cancelar");
        }
    }

    @Nested
    class EditarAplicacaoTests {

        @Test
        void deveEditar_RecalculandoSaldo() {
            stubUsuario(autor);
            EVacinacaoAnimalItem item = new EVacinacaoAnimalItem();
            item.setAnimal(animalAtivo(1L));

            EVacinacaoAnimal vacinacao = new EVacinacaoAnimal();
            vacinacao.setId(900L);
            vacinacao.setInsumo(vacina);
            vacinacao.setUnidadeRegistro(primaria);
            vacinacao.setQuantidadeBaixaPorAnimalUnidadePrimaria(5.0);
            vacinacao.setQuantidadeTotalBaixaUnidadePrimaria(5.0);
            vacinacao.setCriadoPorEmail(autor.getEmail());
            vacinacao.setCancelado(false);
            vacinacao.setItens(List.of(item));

            vacina.setSaldoAtual(995.0);
            when(vacinacaoAnimalInterface.findById(900L)).thenReturn(Optional.of(vacinacao));
            when(vacinacaoAnimalInterface.save(any(EVacinacaoAnimal.class))).thenAnswer(inv -> inv.getArgument(0));

            VacinacaoAnimalEdicaoDto dto = new VacinacaoAnimalEdicaoDto();
            dto.setQuantidadePorAnimal(8.0);

            VacinacaoAnimalRespostaDto resultado = sVacinacaoAnimal.editarAplicacao(900L, dto, autor.getEmail());

            // saldo 995 + estorno(5) - nova baixa(8) = 992
            assertThat(vacina.getSaldoAtual()).isEqualTo(992.0);
            assertThat(resultado.getQuantidadeBaixaPorAnimalUnidadePrimaria()).isEqualTo(8.0);
        }

        @Test
        void deveLancarExcecao_QuandoJaCancelada() {
            stubUsuario(autor);
            EVacinacaoAnimal vacinacao = new EVacinacaoAnimal();
            vacinacao.setId(900L);
            vacinacao.setCancelado(true);
            vacinacao.setCriadoPorEmail(autor.getEmail());
            when(vacinacaoAnimalInterface.findById(900L)).thenReturn(Optional.of(vacinacao));

            assertThatThrownBy(() -> sVacinacaoAnimal.editarAplicacao(
                    900L, new VacinacaoAnimalEdicaoDto(), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("já cancelada");
        }
    }

    @Nested
    class ListarTests {

        @Test
        void listarTodos_deveDelegarParaRepositorio() {
            when(vacinacaoAnimalInterface.findAllByOrderByDataAplicacaoDesc()).thenReturn(List.of());

            assertThat(sVacinacaoAnimal.listarTodos(null, null)).isEmpty();
        }

        @Test
        void listarPorAnimal_deveDelegarParaRepositorio() {
            when(vacinacaoAnimalInterface.findByAnimalId(1L)).thenReturn(List.of());

            assertThat(sVacinacaoAnimal.listarPorAnimal(1L)).isEmpty();
        }
    }
}
