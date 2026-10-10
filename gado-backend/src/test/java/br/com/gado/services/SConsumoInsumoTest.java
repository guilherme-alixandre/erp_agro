package br.com.gado.services;

import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoCadastroDto;
import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoEdicaoDto;
import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoResumoItemDto;
import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoRespostaDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EConsumoInsumo;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.ELote;
import br.com.gado.entities.ELoteSetor;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.ESobraAlimentacao;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoInsumo;
import br.com.gado.enums.EnTipoMovimentacaoEstoque;
import br.com.gado.repositories.IConsumoInsumo;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.ILoteSetor;
import br.com.gado.repositories.ISetor;
import br.com.gado.repositories.ISobraAlimentacao;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SConsumoInsumoTest {

    @InjectMocks
    private SConsumoInsumo sConsumoInsumo;

    @Mock private IConsumoInsumo consumoInsumoInterface;
    @Mock private IInsumo insumoInterface;
    @Mock private ISetor setorInterface;
    @Mock private IUnidadeMedida unidadeMedidaInterface;
    @Mock private ILoteSetor loteSetorInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private SInsumo insumoService;
    @Mock private ISobraAlimentacao sobraAlimentacaoInterface;
    @Mock private SLancamentoFinanceiro lancamentoFinanceiroService;

    private EUnidadeMedida primaria;
    private EInsumo insumo;
    private ESetor setor;
    private EUsuario autor;

    @BeforeEach
    void setUp() {
        primaria = new EUnidadeMedida();
        primaria.setId(1L);
        primaria.setUnidade("SACA");

        insumo = new EInsumo();
        insumo.setId(200L);
        insumo.setNome("Ração Bovina");
        insumo.setTipo(EnTipoInsumo.RACAO);
        insumo.setSaldoAtual(100.0);
        insumo.setUnidadeMedidaPrimaria(primaria);
        insumo.setPrecoCompraMedio(50.0);

        setor = new ESetor();
        setor.setId(10L);
        setor.setNome("Pasto 1");

        autor = new EUsuario();
        autor.setEmail("cuidador@fazenda.com");
        autor.setPerfil(EnPerfilUsuario.CUIDADOR);
    }

    private ConsumoInsumoCadastroDto dtoCadastro(double quantidade) {
        ConsumoInsumoCadastroDto dto = new ConsumoInsumoCadastroDto();
        dto.setInsumoId(insumo.getId());
        dto.setSetorId(setor.getId());
        dto.setQuantidade(quantidade);
        return dto;
    }

    private ELoteSetor alocacaoComAnimais(int quantidadeAnimais) {
        ELote lote = new ELote();
        lote.setStatus(EnStatus.A);
        ELoteSetor ls = new ELoteSetor();
        ls.setLote(lote);
        ls.setSetor(setor);
        for (int i = 0; i < quantidadeAnimais; i++) {
            EAnimal animal = new EAnimal();
            animal.setId((long) (1000 + i));
            ls.getAnimais().add(animal);
        }
        return ls;
    }

    @Nested
    class RegistrarConsumoTests {

        @Test
        void deveRegistrarConsumo_DebitandoEstoqueERateandoPorAnimal() {
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));
            when(setorInterface.findByIdAndStatus(setor.getId(), EnStatus.A)).thenReturn(Optional.of(setor));
            when(loteSetorInterface.findBySetor_Id(setor.getId())).thenReturn(List.of(alocacaoComAnimais(2)));
            when(consumoInsumoInterface.save(any(EConsumoInsumo.class))).thenAnswer(inv -> {
                EConsumoInsumo c = inv.getArgument(0);
                c.setId(900L);
                return c;
            });
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(900L)).thenReturn(Optional.empty());

            ConsumoInsumoRespostaDto resultado = sConsumoInsumo.registrarConsumo(dtoCadastro(10.0), autor.getEmail());

            assertThat(resultado.getQuantidadeBaixaUnidadePrimaria()).isEqualTo(10.0);
            assertThat(resultado.getTotalAnimaisSetor()).isEqualTo(2);
            assertThat(resultado.getConsumoPorAnimal()).isEqualTo(5.0);
            assertThat(insumo.getSaldoAtual()).isEqualTo(90.0);
            verify(insumoInterface).save(insumo);
            verify(insumoService).registrarMovimentacao(eq(insumo), eq(EnTipoMovimentacaoEstoque.SAIDA),
                    eq(10.0), eq(50.0), any(), isNull(), eq(setor), isNull());
        }

        @Test
        void deveRegistrarComConsumoPorAnimalNulo_QuandoSetorSemAnimaisAlocados() {
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));
            when(setorInterface.findByIdAndStatus(setor.getId(), EnStatus.A)).thenReturn(Optional.of(setor));
            when(loteSetorInterface.findBySetor_Id(setor.getId())).thenReturn(List.of());
            when(consumoInsumoInterface.save(any(EConsumoInsumo.class))).thenAnswer(inv -> inv.getArgument(0));
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(any())).thenReturn(Optional.empty());

            ConsumoInsumoRespostaDto resultado = sConsumoInsumo.registrarConsumo(dtoCadastro(10.0), autor.getEmail());

            assertThat(resultado.getTotalAnimaisSetor()).isZero();
            assertThat(resultado.getConsumoPorAnimal()).isNull();
        }

        @Test
        void deveLancarExcecao_QuandoInsumoNaoEncontrado() {
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sConsumoInsumo.registrarConsumo(dtoCadastro(10.0), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insumo não encontrado");
        }

        @Test
        void deveLancarExcecao_QuandoInsumoEhCabecaDeAnimal() {
            insumo.setTipo(EnTipoInsumo.ANIMAL);
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));

            assertThatThrownBy(() -> sConsumoInsumo.registrarConsumo(dtoCadastro(10.0), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("venda no Financeiro");
        }

        @Test
        void deveLancarExcecao_QuandoInsumoEhVacina() {
            insumo.setTipo(EnTipoInsumo.VACINA);
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));

            assertThatThrownBy(() -> sConsumoInsumo.registrarConsumo(dtoCadastro(10.0), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Vacinar Animais");
        }

        @Test
        void deveLancarExcecao_QuandoSetorNaoEncontrado() {
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));
            when(setorInterface.findByIdAndStatus(setor.getId(), EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sConsumoInsumo.registrarConsumo(dtoCadastro(10.0), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Setor não encontrado");
        }

        @Test
        void deveLancarExcecao_QuandoInsumoSemUnidadeMedidaPrimaria() {
            insumo.setUnidadeMedidaPrimaria(null);
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));
            when(setorInterface.findByIdAndStatus(setor.getId(), EnStatus.A)).thenReturn(Optional.of(setor));

            assertThatThrownBy(() -> sConsumoInsumo.registrarConsumo(dtoCadastro(10.0), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("unidade de medida");
        }

        @Test
        void deveLancarExcecao_QuandoDataDeConsumoNoFuturo() {
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));
            when(setorInterface.findByIdAndStatus(setor.getId(), EnStatus.A)).thenReturn(Optional.of(setor));

            ConsumoInsumoCadastroDto dto = dtoCadastro(10.0);
            dto.setDataConsumo(LocalDateTime.now().plusDays(1));

            assertThatThrownBy(() -> sConsumoInsumo.registrarConsumo(dto, autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não pode ser no futuro");
        }

        @Test
        void deveLancarExcecao_QuandoEstoqueInsuficiente() {
            when(insumoInterface.findByIdAndStatus(insumo.getId(), EnStatus.A)).thenReturn(Optional.of(insumo));
            when(setorInterface.findByIdAndStatus(setor.getId(), EnStatus.A)).thenReturn(Optional.of(setor));

            assertThatThrownBy(() -> sConsumoInsumo.registrarConsumo(dtoCadastro(1000.0), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Estoque insuficiente");
            verify(consumoInsumoInterface, never()).save(any());
        }
    }

    @Nested
    class EditarConsumoTests {

        private EConsumoInsumo consumoExistente() {
            EConsumoInsumo consumo = new EConsumoInsumo();
            consumo.setId(900L);
            consumo.setInsumo(insumo);
            consumo.setSetor(setor);
            consumo.setUnidadeRegistro(primaria);
            consumo.setQuantidadeBaixaUnidadePrimaria(10.0);
            consumo.setRegistradoPorEmail(autor.getEmail());
            consumo.setStatus(EnStatus.A);
            return consumo;
        }

        @Test
        void deveEditarConsumo_RecalculandoSaldo() {
            EConsumoInsumo consumo = consumoExistente();
            when(usuarioInterface.findByEmailAndStatus(autor.getEmail(), EnStatus.A)).thenReturn(Optional.of(autor));
            when(consumoInsumoInterface.findById(900L)).thenReturn(Optional.of(consumo));
            when(loteSetorInterface.findBySetor_Id(setor.getId())).thenReturn(List.of(alocacaoComAnimais(1)));
            when(consumoInsumoInterface.save(any(EConsumoInsumo.class))).thenAnswer(inv -> inv.getArgument(0));
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(900L)).thenReturn(Optional.empty());

            ConsumoInsumoEdicaoDto dto = new ConsumoInsumoEdicaoDto();
            dto.setQuantidade(20.0);

            ConsumoInsumoRespostaDto resultado = sConsumoInsumo.editarConsumo(900L, dto, autor.getEmail());

            // saldo 100 + estorno(10) - nova baixa(20) = 90
            assertThat(insumo.getSaldoAtual()).isEqualTo(90.0);
            assertThat(resultado.getQuantidadeBaixaUnidadePrimaria()).isEqualTo(20.0);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoConsumoNaoExiste() {
            when(usuarioInterface.findByEmailAndStatus(autor.getEmail(), EnStatus.A)).thenReturn(Optional.of(autor));
            when(consumoInsumoInterface.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sConsumoInsumo.editarConsumo(999L, new ConsumoInsumoEdicaoDto(), autor.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioSemPermissaoDeEdicao() {
            EConsumoInsumo consumo = consumoExistente();
            EUsuario outroUsuario = new EUsuario();
            outroUsuario.setEmail("outro@fazenda.com");
            outroUsuario.setPerfil(EnPerfilUsuario.CUIDADOR);

            when(usuarioInterface.findByEmailAndStatus(outroUsuario.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(outroUsuario));
            when(consumoInsumoInterface.findById(900L)).thenReturn(Optional.of(consumo));

            assertThatThrownBy(() -> sConsumoInsumo.editarConsumo(900L, new ConsumoInsumoEdicaoDto(), outroUsuario.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("só pode editar");
        }

        @Test
        void deveLancarExcecao_QuandoEstoqueInsuficienteAposEstorno() {
            EConsumoInsumo consumo = consumoExistente();
            when(usuarioInterface.findByEmailAndStatus(autor.getEmail(), EnStatus.A)).thenReturn(Optional.of(autor));
            when(consumoInsumoInterface.findById(900L)).thenReturn(Optional.of(consumo));

            ConsumoInsumoEdicaoDto dto = new ConsumoInsumoEdicaoDto();
            dto.setQuantidade(100000.0);

            assertThatThrownBy(() -> sConsumoInsumo.editarConsumo(900L, dto, autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Estoque insuficiente");
        }
    }

    @Nested
    class ListarPorSetorTests {

        @Test
        void deveListarSemFiltroDeData_QuandoDatasNaoInformadas() {
            when(consumoInsumoInterface.findBySetor_IdAndStatusOrderByDataConsumoDesc(setor.getId(), EnStatus.A))
                    .thenReturn(List.of());
            when(sobraAlimentacaoInterface.findByConsumoInsumo_IdIn(List.of())).thenReturn(List.of());

            List<ConsumoInsumoRespostaDto> resultado = sConsumoInsumo.listarPorSetor(setor.getId(), null, null);

            assertThat(resultado).isEmpty();
            verify(consumoInsumoInterface, never())
                    .findBySetor_IdAndStatusAndDataConsumoBetweenOrderByDataConsumoDesc(any(), any(), any(), any());
        }

        @Test
        void deveListarComFiltroDeData_QuandoDatasInformadas() {
            LocalDate inicio = LocalDate.of(2026, 1, 1);
            LocalDate fim = LocalDate.of(2026, 1, 31);
            when(consumoInsumoInterface.findBySetor_IdAndStatusAndDataConsumoBetweenOrderByDataConsumoDesc(
                    eq(setor.getId()), eq(EnStatus.A), any(), any())).thenReturn(List.of());
            when(sobraAlimentacaoInterface.findByConsumoInsumo_IdIn(List.of())).thenReturn(List.of());

            List<ConsumoInsumoRespostaDto> resultado = sConsumoInsumo.listarPorSetor(setor.getId(), inicio, fim);

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    class ExcluirConsumoTests {

        private EConsumoInsumo consumoExistente() {
            EConsumoInsumo consumo = new EConsumoInsumo();
            consumo.setId(900L);
            consumo.setInsumo(insumo);
            consumo.setSetor(setor);
            consumo.setQuantidadeBaixaUnidadePrimaria(10.0);
            consumo.setRegistradoPorEmail(autor.getEmail());
            consumo.setStatus(EnStatus.A);
            return consumo;
        }

        @Test
        void deveExcluirEDevolverEstoque_QuandoSemSobraRegistrada() {
            EConsumoInsumo consumo = consumoExistente();
            when(usuarioInterface.findByEmailAndStatus(autor.getEmail(), EnStatus.A)).thenReturn(Optional.of(autor));
            when(consumoInsumoInterface.findById(900L)).thenReturn(Optional.of(consumo));
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(900L)).thenReturn(Optional.empty());

            sConsumoInsumo.excluirConsumo(900L, autor.getEmail());

            assertThat(insumo.getSaldoAtual()).isEqualTo(110.0);
            assertThat(consumo.getStatus()).isEqualTo(EnStatus.I);
            verifyNoInteractions(lancamentoFinanceiroService);
        }

        @Test
        void deveEstornarPerda_QuandoHaviaSobraNaoReaproveitada() {
            EConsumoInsumo consumo = consumoExistente();
            ESobraAlimentacao sobra = new ESobraAlimentacao();
            sobra.setId(700L);
            sobra.setReaproveitado(false);

            when(usuarioInterface.findByEmailAndStatus(autor.getEmail(), EnStatus.A)).thenReturn(Optional.of(autor));
            when(consumoInsumoInterface.findById(900L)).thenReturn(Optional.of(consumo));
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(900L)).thenReturn(Optional.of(sobra));

            sConsumoInsumo.excluirConsumo(900L, autor.getEmail());

            verify(lancamentoFinanceiroService).estornarPerdaAlimentacao(700L);
            verify(sobraAlimentacaoInterface).delete(sobra);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoConsumoNaoExiste() {
            when(usuarioInterface.findByEmailAndStatus(autor.getEmail(), EnStatus.A)).thenReturn(Optional.of(autor));
            when(consumoInsumoInterface.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sConsumoInsumo.excluirConsumo(999L, autor.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ResumoPorSetorEPeriodoTests {

        @Test
        void deveAgregarTotalPorInsumo() {
            IConsumoInsumo.ResumoInsumoProjection projecao = mock(IConsumoInsumo.ResumoInsumoProjection.class);
            when(projecao.getInsumoId()).thenReturn(insumo.getId());
            when(projecao.getTotal()).thenReturn(15.0);

            LocalDate inicio = LocalDate.of(2026, 1, 1);
            LocalDate fim = LocalDate.of(2026, 1, 31);
            when(consumoInsumoInterface.resumoPorSetorEPeriodo(eq(setor.getId()), eq(EnStatus.A), any(), any()))
                    .thenReturn(List.of(projecao));
            when(insumoInterface.findAllById(List.of(insumo.getId()))).thenReturn(List.of(insumo));

            List<ConsumoInsumoResumoItemDto> resultado = sConsumoInsumo.resumoPorSetorEPeriodo(setor.getId(), inicio, fim);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getInsumoNome()).isEqualTo("Ração Bovina");
            assertThat(resultado.get(0).getQuantidadeTotal()).isEqualTo(15.0);
            assertThat(resultado.get(0).getUnidadeSigla()).isEqualTo("SACA");
        }
    }
}
