package br.com.gado.services;

import br.com.gado.dto.documentoSaidaDto.DocumentoSaidaRespostaDto;
import br.com.gado.dto.documentoSaidaDto.VendaAnimalCadastroDto;
import br.com.gado.dto.documentoSaidaDto.VendaLeiteCadastroDto;
import br.com.gado.dto.documentoSaidaDto.VendaLeiteItemCadastroDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EDocumentoSaida;
import br.com.gado.entities.ELote;
import br.com.gado.entities.ELoteSetor;
import br.com.gado.entities.EMetaSetor;
import br.com.gado.entities.EParceiro;
import br.com.gado.entities.ERaca;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnStatusAnimal;
import br.com.gado.enums.EnTipoMeta;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.IDocumentoSaida;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.ILoteSetor;
import br.com.gado.repositories.IMetaSetor;
import br.com.gado.repositories.IParceiro;
import br.com.gado.repositories.IUsuario;
import br.com.gado.repositories.IVendaMetaLote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SDocumentoSaidaTest {

    @InjectMocks
    private SDocumentoSaida sDocumentoSaida;

    @Mock private IDocumentoSaida documentoSaidaInterface;
    @Mock private ILote loteInterface;
    @Mock private ILoteSetor loteSetorInterface;
    @Mock private IAnimal animalInterface;
    @Mock private IMetaSetor metaSetorInterface;
    @Mock private IVendaMetaLote vendaMetaLoteInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private IParceiro parceiroInterface;
    @Mock private SLancamentoFinanceiro lancamentoFinanceiroService;
    @Mock private SInsumo insumoService;

    private EUsuario financeiro;
    private EParceiro comprador;

    @BeforeEach
    void setUp() {
        financeiro = new EUsuario();
        financeiro.setEmail("financeiro@fazenda.com");
        financeiro.setPerfil(EnPerfilUsuario.FINANCEIRO);

        comprador = new EParceiro();
        comprador.setId(1L);
        comprador.setNome("Frigorífico Teste");
    }

    private void stubUsuario(EUsuario usuario) {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(usuario));
    }

    @Nested
    class CadastrarVendaLeiteTests {

        private VendaLeiteItemCadastroDto item(Long loteId, String litros) {
            VendaLeiteItemCadastroDto item = new VendaLeiteItemCadastroDto();
            item.setLoteId(loteId);
            item.setLitros(new BigDecimal(litros));
            return item;
        }

        @Test
        void deveCalcularPrecoLitro_QuandoValorTotalInformado() {
            stubUsuario(financeiro);
            ELote lote = new ELote();
            lote.setId(10L);
            lote.setCodigo("LOT001");
            when(loteInterface.findByIdAndStatus(10L, EnStatus.A)).thenReturn(Optional.of(lote));
            when(parceiroInterface.findById(1L)).thenReturn(Optional.of(comprador));
            when(loteSetorInterface.findByLote_Id(10L)).thenReturn(List.of());
            when(documentoSaidaInterface.save(any(EDocumentoSaida.class))).thenAnswer(inv -> inv.getArgument(0));

            VendaLeiteCadastroDto dto = new VendaLeiteCadastroDto();
            dto.setDataEmissao(LocalDate.of(2026, 3, 1));
            dto.setCompradorId(1L);
            dto.setValorTotal(new BigDecimal("200.00"));
            dto.setItens(List.of(item(10L, "100")));

            DocumentoSaidaRespostaDto resultado = sDocumentoSaida.cadastrarVendaLeite(dto, financeiro.getEmail());

            assertThat(resultado.getItensLeite().get(0).getPrecoLitro()).isEqualByComparingTo("2.0000");
            assertThat(resultado.getValorTotal()).isEqualByComparingTo("200.00");
            assertThat(resultado.getFormaPagamento()).isEqualTo(br.com.gado.enums.EnFormaPagamento.A_VISTA);
            assertThat(resultado.getDataVencimento()).isEqualTo(resultado.getDataEmissao());
            assertThat(resultado.getNumeroParcelas()).isEqualTo(1);
            verify(lancamentoFinanceiroService).registrarEntradaVenda(any(), any(), any(), any(), eq(financeiro.getEmail()));
        }

        @Test
        void deveCadastrarParcelado_QuandoFormaPagamentoInformada() {
            stubUsuario(financeiro);
            ELote lote = new ELote();
            lote.setId(10L);
            when(loteInterface.findByIdAndStatus(10L, EnStatus.A)).thenReturn(Optional.of(lote));
            when(parceiroInterface.findById(1L)).thenReturn(Optional.of(comprador));
            when(loteSetorInterface.findByLote_Id(10L)).thenReturn(List.of());
            when(documentoSaidaInterface.save(any(EDocumentoSaida.class))).thenAnswer(inv -> inv.getArgument(0));

            VendaLeiteCadastroDto dto = new VendaLeiteCadastroDto();
            dto.setDataEmissao(LocalDate.of(2026, 3, 1));
            dto.setCompradorId(1L);
            dto.setValorTotal(new BigDecimal("200.00"));
            dto.setItens(List.of(item(10L, "100")));
            dto.setFormaPagamento(br.com.gado.enums.EnFormaPagamento.PARCELADO);
            dto.setDataVencimento(LocalDate.of(2026, 4, 1));
            dto.setNumeroParcelas(2);
            dto.setValorParcela(new BigDecimal("100.00"));

            DocumentoSaidaRespostaDto resultado = sDocumentoSaida.cadastrarVendaLeite(dto, financeiro.getEmail());

            assertThat(resultado.getFormaPagamento()).isEqualTo(br.com.gado.enums.EnFormaPagamento.PARCELADO);
            assertThat(resultado.getDataVencimento()).isEqualTo(LocalDate.of(2026, 4, 1));
            assertThat(resultado.getNumeroParcelas()).isEqualTo(2);
            assertThat(resultado.getValorParcela()).isEqualByComparingTo("100.00");
        }

        @Test
        void deveCalcularValorTotal_QuandoPrecoLitroInformado() {
            stubUsuario(financeiro);
            ELote lote = new ELote();
            lote.setId(10L);
            when(loteInterface.findByIdAndStatus(10L, EnStatus.A)).thenReturn(Optional.of(lote));
            when(parceiroInterface.findById(1L)).thenReturn(Optional.of(comprador));
            when(loteSetorInterface.findByLote_Id(10L)).thenReturn(List.of());
            when(documentoSaidaInterface.save(any(EDocumentoSaida.class))).thenAnswer(inv -> inv.getArgument(0));

            VendaLeiteCadastroDto dto = new VendaLeiteCadastroDto();
            dto.setDataEmissao(LocalDate.of(2026, 3, 1));
            dto.setCompradorId(1L);
            dto.setPrecoLitro(new BigDecimal("2.50"));
            dto.setItens(List.of(item(10L, "100")));

            DocumentoSaidaRespostaDto resultado = sDocumentoSaida.cadastrarVendaLeite(dto, financeiro.getEmail());

            assertThat(resultado.getValorTotal()).isEqualByComparingTo("250.00");
        }

        @Test
        void deveLancarExcecao_QuandoNemPrecoNemValorInformado() {
            stubUsuario(financeiro);

            VendaLeiteCadastroDto dto = new VendaLeiteCadastroDto();
            dto.setDataEmissao(LocalDate.of(2026, 3, 1));
            dto.setCompradorId(1L);
            dto.setItens(List.of(item(10L, "100")));

            assertThatThrownBy(() -> sDocumentoSaida.cadastrarVendaLeite(dto, financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("preço por litro ou o valor total");
        }

        @Test
        void deveLancarExcecao_QuandoLoteNaoEncontrado() {
            stubUsuario(financeiro);
            when(parceiroInterface.findById(1L)).thenReturn(Optional.of(comprador));
            when(loteInterface.findByIdAndStatus(10L, EnStatus.A)).thenReturn(Optional.empty());

            VendaLeiteCadastroDto dto = new VendaLeiteCadastroDto();
            dto.setDataEmissao(LocalDate.of(2026, 3, 1));
            dto.setCompradorId(1L);
            dto.setPrecoLitro(new BigDecimal("2.50"));
            dto.setItens(List.of(item(10L, "100")));

            assertThatThrownBy(() -> sDocumentoSaida.cadastrarVendaLeite(dto, financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Lote não encontrado");
        }

        @Test
        void deveRegistrarVendidoNaMeta_QuandoMetaLeiteAtivaCobreData() {
            stubUsuario(financeiro);
            ELote lote = new ELote();
            lote.setId(10L);
            when(loteInterface.findByIdAndStatus(10L, EnStatus.A)).thenReturn(Optional.of(lote));
            when(parceiroInterface.findById(1L)).thenReturn(Optional.of(comprador));
            when(documentoSaidaInterface.save(any(EDocumentoSaida.class))).thenAnswer(inv -> inv.getArgument(0));

            ESetor setor = new ESetor();
            setor.setId(20L);
            ELoteSetor alocacao = new ELoteSetor();
            alocacao.setSetor(setor);
            when(loteSetorInterface.findByLote_Id(10L)).thenReturn(List.of(alocacao));

            EMetaSetor meta = new EMetaSetor();
            meta.setTipoMeta(EnTipoMeta.LEITE);
            meta.setDataInicial(LocalDate.of(2026, 1, 1));
            meta.setDataFinal(LocalDate.of(2026, 12, 31));
            when(metaSetorInterface.findBySetor_IdAndStatus(20L, EnStatus.A)).thenReturn(List.of(meta));

            VendaLeiteCadastroDto dto = new VendaLeiteCadastroDto();
            dto.setDataEmissao(LocalDate.of(2026, 3, 1));
            dto.setCompradorId(1L);
            dto.setPrecoLitro(new BigDecimal("2.50"));
            dto.setItens(List.of(item(10L, "100")));

            sDocumentoSaida.cadastrarVendaLeite(dto, financeiro.getEmail());

            verify(vendaMetaLoteInterface).save(any());
        }

        @Test
        void naoDeveRegistrarNaMeta_QuandoDataForaDoPeriodoDaMeta() {
            stubUsuario(financeiro);
            ELote lote = new ELote();
            lote.setId(10L);
            when(loteInterface.findByIdAndStatus(10L, EnStatus.A)).thenReturn(Optional.of(lote));
            when(parceiroInterface.findById(1L)).thenReturn(Optional.of(comprador));
            when(documentoSaidaInterface.save(any(EDocumentoSaida.class))).thenAnswer(inv -> inv.getArgument(0));

            ESetor setor = new ESetor();
            setor.setId(20L);
            ELoteSetor alocacao = new ELoteSetor();
            alocacao.setSetor(setor);
            when(loteSetorInterface.findByLote_Id(10L)).thenReturn(List.of(alocacao));

            EMetaSetor meta = new EMetaSetor();
            meta.setTipoMeta(EnTipoMeta.LEITE);
            meta.setDataInicial(LocalDate.of(2025, 1, 1));
            meta.setDataFinal(LocalDate.of(2025, 12, 31));
            when(metaSetorInterface.findBySetor_IdAndStatus(20L, EnStatus.A)).thenReturn(List.of(meta));

            VendaLeiteCadastroDto dto = new VendaLeiteCadastroDto();
            dto.setDataEmissao(LocalDate.of(2026, 3, 1));
            dto.setCompradorId(1L);
            dto.setPrecoLitro(new BigDecimal("2.50"));
            dto.setItens(List.of(item(10L, "100")));

            sDocumentoSaida.cadastrarVendaLeite(dto, financeiro.getEmail());

            verify(vendaMetaLoteInterface, never()).save(any());
        }
    }

    @Nested
    class CadastrarVendaAnimalTests {

        private VendaAnimalCadastroDto dtoBase(EnStatusAnimal destino, Long... animalIds) {
            VendaAnimalCadastroDto dto = new VendaAnimalCadastroDto();
            dto.setDataEmissao(LocalDate.of(2026, 3, 1));
            dto.setCompradorId(1L);
            dto.setDestino(destino);
            dto.setAnimalIds(List.of(animalIds));
            dto.setValorTotal(new BigDecimal("2000.00"));
            return dto;
        }

        private EAnimal animalAtivo(Long id) {
            EAnimal animal = new EAnimal();
            animal.setId(id);
            animal.setCodigoBrinco("BR-" + id);
            animal.setStatusAnimal(EnStatusAnimal.ATIVO);
            return animal;
        }

        @Test
        void deveVenderAnimais_DividindoValorIgualmenteEntreEles() {
            stubUsuario(financeiro);
            EAnimal a1 = animalAtivo(1L);
            EAnimal a2 = animalAtivo(2L);
            when(animalInterface.findAllById(List.of(1L, 2L))).thenReturn(List.of(a1, a2));
            when(parceiroInterface.findById(1L)).thenReturn(Optional.of(comprador));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(anyLong(), eq(EnStatus.A))).thenReturn(List.of());
            when(animalInterface.save(any(EAnimal.class))).thenAnswer(inv -> inv.getArgument(0));
            when(documentoSaidaInterface.save(any(EDocumentoSaida.class))).thenAnswer(inv -> inv.getArgument(0));

            DocumentoSaidaRespostaDto resultado =
                    sDocumentoSaida.cadastrarVendaAnimal(dtoBase(EnStatusAnimal.VENDIDO, 1L, 2L), financeiro.getEmail());

            assertThat(resultado.getItensAnimal()).hasSize(2);
            assertThat(resultado.getItensAnimal().get(0).getValorVenda()).isEqualByComparingTo("1000.00");
            assertThat(a1.getStatusAnimal()).isEqualTo(EnStatusAnimal.VENDIDO);
            assertThat(a2.getStatusAnimal()).isEqualTo(EnStatusAnimal.VENDIDO);
            assertThat(resultado.getFormaPagamento()).isEqualTo(br.com.gado.enums.EnFormaPagamento.A_VISTA);
            assertThat(resultado.getNumeroParcelas()).isEqualTo(1);
        }

        @Test
        void deveLiberarAnimalDeAlocacaoDeSetor_AoVender() {
            stubUsuario(financeiro);
            EAnimal animal = animalAtivo(1L);
            when(animalInterface.findAllById(List.of(1L))).thenReturn(List.of(animal));
            when(parceiroInterface.findById(1L)).thenReturn(Optional.of(comprador));

            ELoteSetor alocacao = new ELoteSetor();
            alocacao.getAnimais().add(animal);
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(1L, EnStatus.A)).thenReturn(List.of(alocacao));
            when(animalInterface.save(any(EAnimal.class))).thenAnswer(inv -> inv.getArgument(0));
            when(documentoSaidaInterface.save(any(EDocumentoSaida.class))).thenAnswer(inv -> inv.getArgument(0));

            sDocumentoSaida.cadastrarVendaAnimal(dtoBase(EnStatusAnimal.ABATIDO, 1L), financeiro.getEmail());

            assertThat(alocacao.getAnimais()).isEmpty();
            verify(loteSetorInterface).save(alocacao);
        }

        @Test
        void deveBaixarEstoqueDoProdutoDaRaca_QuandoRacaVinculadaAUmProduto() {
            stubUsuario(financeiro);
            EAnimal animal = animalAtivo(1L);
            ERaca raca = new ERaca();
            var produto = new br.com.gado.entities.EInsumo();
            produto.setId(500L);
            raca.setProduto(produto);
            animal.setRaca(raca);

            when(animalInterface.findAllById(List.of(1L))).thenReturn(List.of(animal));
            when(parceiroInterface.findById(1L)).thenReturn(Optional.of(comprador));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(1L, EnStatus.A)).thenReturn(List.of());
            when(animalInterface.save(any(EAnimal.class))).thenAnswer(inv -> inv.getArgument(0));
            when(documentoSaidaInterface.save(any(EDocumentoSaida.class))).thenAnswer(inv -> inv.getArgument(0));

            sDocumentoSaida.cadastrarVendaAnimal(dtoBase(EnStatusAnimal.VENDIDO, 1L), financeiro.getEmail());

            verify(insumoService).baixarEstoque(eq(500L), eq(1.0), eq(animal), any());
        }

        @Test
        void deveLancarExcecao_QuandoDestinoInvalido() {
            stubUsuario(financeiro);

            assertThatThrownBy(() -> sDocumentoSaida.cadastrarVendaAnimal(
                    dtoBase(EnStatusAnimal.ATIVO, 1L), financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Destino inválido");
        }

        @Test
        void deveLancarExcecao_QuandoAnimalNaoEncontrado() {
            stubUsuario(financeiro);
            when(animalInterface.findAllById(List.of(1L))).thenReturn(List.of());

            assertThatThrownBy(() -> sDocumentoSaida.cadastrarVendaAnimal(
                    dtoBase(EnStatusAnimal.VENDIDO, 1L), financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não foram encontrados");
        }

        @Test
        void deveLancarExcecao_QuandoAnimalJaVendido() {
            stubUsuario(financeiro);
            EAnimal animal = animalAtivo(1L);
            animal.setStatusAnimal(EnStatusAnimal.VENDIDO);
            when(animalInterface.findAllById(List.of(1L))).thenReturn(List.of(animal));

            assertThatThrownBy(() -> sDocumentoSaida.cadastrarVendaAnimal(
                    dtoBase(EnStatusAnimal.VENDIDO, 1L), financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não pode ser vendido/abatido");
        }
    }

    @Nested
    class ListarTodosTests {

        @Test
        void deveListarDocumentosDeSaidaAtivos() {
            stubUsuario(financeiro);
            EDocumentoSaida documento = new EDocumentoSaida();
            documento.setItensLeite(List.of());
            documento.setItensAnimal(List.of());
            when(documentoSaidaInterface.findByStatusOrderByDataEmissaoDesc(EnStatus.A))
                    .thenReturn(List.of(documento));

            assertThat(sDocumentoSaida.listarTodos(financeiro.getEmail())).hasSize(1);
        }
    }
}
