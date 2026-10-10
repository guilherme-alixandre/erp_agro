package br.com.gado.services;

import br.com.gado.dto.documentoEntradaDto.DocumentoEntradaRespostaDto;
import br.com.gado.dto.documentoEntradaDto.NfeItemUpdateDto;
import br.com.gado.dto.documentoEntradaDto.NfeUpdateDto;
import br.com.gado.dto.documentoEntradaDto.ReciboSimplesCadastroDto;
import br.com.gado.dto.documentoEntradaDto.RecusaDocumentoDto;
import br.com.gado.dto.documentoEntradaDto.VincularProdutoDto;
import br.com.gado.entities.EDocumentoEntrada;
import br.com.gado.entities.EDocumentoEntradaItem;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.EParceiro;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnStatusAprovacaoFinanceira;
import br.com.gado.enums.EnTipoDocumentoFinanceiro;
import br.com.gado.repositories.IDocumentoEntrada;
import br.com.gado.repositories.IDocumentoEntradaItem;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.IParceiro;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SDocumentoEntradaTest {

    @InjectMocks
    private SDocumentoEntrada sDocumentoEntrada;

    @Mock private IDocumentoEntrada documentoInterface;
    @Mock private IDocumentoEntradaItem itemInterface;
    @Mock private IParceiro parceiroInterface;
    @Mock private IInsumo insumoInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private SLancamentoFinanceiro lancamentoFinanceiroService;
    @Mock private SInsumo insumoService;

    private EUsuario admin;
    private EUsuario financeiro;
    private EParceiro fornecedor;

    @BeforeEach
    void setUp() {
        admin = new EUsuario();
        admin.setEmail("admin@fazenda.com");
        admin.setPerfil(EnPerfilUsuario.ADMINISTRADOR);
        admin.setSenha(new BCryptPasswordEncoder().encode("senha123"));

        financeiro = new EUsuario();
        financeiro.setEmail("financeiro@fazenda.com");
        financeiro.setPerfil(EnPerfilUsuario.FINANCEIRO);

        fornecedor = new EParceiro();
        fornecedor.setId(1L);
        fornecedor.setNome("Agropecuária Teste");
    }

    private void stubUsuario(EUsuario usuario) {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(usuario));
    }

    private byte[] xmlNfeValida() {
        try (InputStream in = SDocumentoEntradaTest.class.getResourceAsStream("/nfe/nfe-racao-valida.xml")) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Nested
    class ImportarNfeXmlTests {

        @Test
        void deveImportarNfe_ComSucesso() throws IOException {
            stubUsuario(financeiro);
            when(parceiroInterface.findById(1L)).thenReturn(Optional.of(fornecedor));
            when(documentoInterface.findByChaveAcessoNfe(any())).thenReturn(Optional.empty());
            when(documentoInterface.save(any(EDocumentoEntrada.class))).thenAnswer(inv -> inv.getArgument(0));

            MockMultipartFile file = new MockMultipartFile("file", "nfe.xml", "text/xml", xmlNfeValida());

            DocumentoEntradaRespostaDto resultado =
                    sDocumentoEntrada.importarNfeXml(file, 1L, financeiro.getEmail());

            assertThat(resultado.getTipoDocumento()).isEqualTo(EnTipoDocumentoFinanceiro.NF_E);
            assertThat(resultado.getStatusAprovacao()).isEqualTo(EnStatusAprovacaoFinanceira.APROVADO);
            assertThat(resultado.getChaveAcessoNfe()).hasSize(44);
            assertThat(resultado.getItens()).hasSize(1);
            verify(lancamentoFinanceiroService).registrarOuAtualizarSaidaItemDocumento(any(), eq(financeiro.getEmail()));
        }

        @Test
        void deveLancarExcecao_QuandoArquivoVazio() {
            stubUsuario(financeiro);
            MockMultipartFile arquivoVazio = new MockMultipartFile("file", "nfe.xml", "text/xml", new byte[0]);

            assertThatThrownBy(() -> sDocumentoEntrada.importarNfeXml(arquivoVazio, 1L, financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Selecione o arquivo");
        }

        @Test
        void deveLancarExcecao_QuandoFornecedorIdNulo() {
            stubUsuario(financeiro);
            MockMultipartFile file = new MockMultipartFile("file", "nfe.xml", "text/xml", xmlNfeValida());

            assertThatThrownBy(() -> sDocumentoEntrada.importarNfeXml(file, null, financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Selecione o fornecedor");
        }

        @Test
        void deveLancarExcecao_QuandoFornecedorNaoEncontrado() {
            stubUsuario(financeiro);
            when(parceiroInterface.findById(99L)).thenReturn(Optional.empty());
            MockMultipartFile file = new MockMultipartFile("file", "nfe.xml", "text/xml", xmlNfeValida());

            assertThatThrownBy(() -> sDocumentoEntrada.importarNfeXml(file, 99L, financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Fornecedor não encontrado");
        }

        @Test
        void deveLancarExcecao_QuandoChaveDeAcessoJaImportada() {
            stubUsuario(financeiro);
            when(parceiroInterface.findById(1L)).thenReturn(Optional.of(fornecedor));
            when(documentoInterface.findByChaveAcessoNfe(any())).thenReturn(Optional.of(new EDocumentoEntrada()));
            MockMultipartFile file = new MockMultipartFile("file", "nfe.xml", "text/xml", xmlNfeValida());

            assertThatThrownBy(() -> sDocumentoEntrada.importarNfeXml(file, 1L, financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("já foi importada");
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioSemPermissao() {
            EUsuario cuidador = new EUsuario();
            cuidador.setEmail("cuidador@fazenda.com");
            cuidador.setPerfil(EnPerfilUsuario.CUIDADOR);
            stubUsuario(cuidador);
            MockMultipartFile file = new MockMultipartFile("file", "nfe.xml", "text/xml", xmlNfeValida());

            assertThatThrownBy(() -> sDocumentoEntrada.importarNfeXml(file, 1L, cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(parceiroInterface);
        }
    }

    @Nested
    class CadastrarReciboSimplesTests {

        private ReciboSimplesCadastroDto dtoValido() {
            ReciboSimplesCadastroDto dto = new ReciboSimplesCadastroDto();
            dto.setDescricao("Prestação de serviço veterinário");
            dto.setDataEmissao(LocalDate.of(2026, 3, 1));
            dto.setProdutoId(200L);
            dto.setQuantidade(new BigDecimal("1"));
            dto.setValorTotal(new BigDecimal("150.00"));
            dto.setNaturezaFinanceira(EnNaturezaFinanceira.GASTO);
            return dto;
        }

        @Test
        void deveCadastrarRecibo_SempreComoPendenteEmAprovacao() {
            stubUsuario(financeiro);
            EInsumo produto = new EInsumo();
            produto.setId(200L);
            when(insumoInterface.findByIdAndStatus(200L, EnStatus.A)).thenReturn(Optional.of(produto));
            when(documentoInterface.save(any(EDocumentoEntrada.class))).thenAnswer(inv -> inv.getArgument(0));

            DocumentoEntradaRespostaDto resultado =
                    sDocumentoEntrada.cadastrarReciboSimples(dtoValido(), financeiro.getEmail());

            assertThat(resultado.getStatusAprovacao()).isEqualTo(EnStatusAprovacaoFinanceira.PENDENTE_APROVACAO);
            assertThat(resultado.getTipoDocumento()).isEqualTo(EnTipoDocumentoFinanceiro.RECIBO_SIMPLES);
            verifyNoInteractions(lancamentoFinanceiroService, insumoService);
        }

        @Test
        void deveLancarExcecao_QuandoProdutoNaoEncontrado() {
            stubUsuario(financeiro);
            when(insumoInterface.findByIdAndStatus(200L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sDocumentoEntrada.cadastrarReciboSimples(dtoValido(), financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class AprovarDocumentoTests {

        private EDocumentoEntrada documentoPendente() {
            EDocumentoEntrada documento = new EDocumentoEntrada();
            documento.setId(300L);
            documento.setStatusAprovacao(EnStatusAprovacaoFinanceira.PENDENTE_APROVACAO);
            documento.setDataEmissao(LocalDate.of(2026, 3, 1));
            documento.setItens(List.of());
            return documento;
        }

        @Test
        void deveAprovarDocumentoPendente() {
            stubUsuario(admin);
            EDocumentoEntrada documento = documentoPendente();
            when(documentoInterface.findById(300L)).thenReturn(Optional.of(documento));
            when(documentoInterface.save(any(EDocumentoEntrada.class))).thenAnswer(inv -> inv.getArgument(0));

            DocumentoEntradaRespostaDto resultado = sDocumentoEntrada.aprovarDocumento(300L, admin.getEmail());

            assertThat(resultado.getStatusAprovacao()).isEqualTo(EnStatusAprovacaoFinanceira.APROVADO);
            assertThat(resultado.getAprovadoPorEmail()).isEqualTo(admin.getEmail());
        }

        @Test
        void deveLancarIllegalStateException_QuandoDocumentoJaAprovado() {
            stubUsuario(admin);
            EDocumentoEntrada documento = documentoPendente();
            documento.setStatusAprovacao(EnStatusAprovacaoFinanceira.APROVADO);
            when(documentoInterface.findById(300L)).thenReturn(Optional.of(documento));

            assertThatThrownBy(() -> sDocumentoEntrada.aprovarDocumento(300L, admin.getEmail()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoDocumentoNaoExiste() {
            stubUsuario(admin);
            when(documentoInterface.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sDocumentoEntrada.aprovarDocumento(999L, admin.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ExcluirDocumentoTests {

        @Test
        void deveExcluirDocumento_RevertendoEstoqueELancamento() {
            stubUsuario(admin);
            EInsumo produto = new EInsumo();
            produto.setId(200L);
            produto.setNome("Ração");
            produto.setSaldoAtual(50.0);

            EDocumentoEntradaItem item = new EDocumentoEntradaItem();
            item.setId(1L);
            item.setProduto(produto);
            item.setEntradaEstoqueAplicada(true);
            item.setQuantidade(new BigDecimal("10"));
            item.setValorUnitario(new BigDecimal("5.00"));

            EDocumentoEntrada documento = new EDocumentoEntrada();
            documento.setId(300L);
            documento.setStatus(EnStatus.A);
            documento.setItens(List.of(item));
            when(documentoInterface.findById(300L)).thenReturn(Optional.of(documento));
            when(insumoInterface.save(any(EInsumo.class))).thenAnswer(inv -> inv.getArgument(0));
            when(documentoInterface.save(any(EDocumentoEntrada.class))).thenAnswer(inv -> inv.getArgument(0));

            String resultado = sDocumentoEntrada.excluirDocumento(300L, admin.getEmail());

            assertThat(resultado).contains("excluído com sucesso");
            assertThat(produto.getSaldoAtual()).isEqualTo(40.0);
            assertThat(documento.getStatus()).isEqualTo(EnStatus.I);
            verify(lancamentoFinanceiroService).estornarSaidaItemDocumento(item);
        }

        @Test
        void deveLancarExcecao_QuandoSaldoJaConsumidoNaoComportaReversao() {
            stubUsuario(admin);
            EInsumo produto = new EInsumo();
            produto.setId(200L);
            produto.setNome("Ração");
            produto.setSaldoAtual(5.0);

            EDocumentoEntradaItem item = new EDocumentoEntradaItem();
            item.setId(1L);
            item.setProduto(produto);
            item.setEntradaEstoqueAplicada(true);
            item.setQuantidade(new BigDecimal("10"));
            item.setValorUnitario(new BigDecimal("5.00"));

            EDocumentoEntrada documento = new EDocumentoEntrada();
            documento.setId(300L);
            documento.setStatus(EnStatus.A);
            documento.setItens(List.of(item));
            when(documentoInterface.findById(300L)).thenReturn(Optional.of(documento));

            assertThatThrownBy(() -> sDocumentoEntrada.excluirDocumento(300L, admin.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("já teve parte deste estoque consumida");
            verify(documentoInterface, never()).save(any());
        }
    }

    @Nested
    class RecusarDocumentoTests {

        @Test
        void deveRecusarDocumentoPendente() {
            stubUsuario(admin);
            EDocumentoEntrada documento = new EDocumentoEntrada();
            documento.setId(300L);
            documento.setStatusAprovacao(EnStatusAprovacaoFinanceira.PENDENTE_APROVACAO);
            documento.setItens(List.of());
            when(documentoInterface.findById(300L)).thenReturn(Optional.of(documento));
            when(documentoInterface.save(any(EDocumentoEntrada.class))).thenAnswer(inv -> inv.getArgument(0));

            RecusaDocumentoDto dto = new RecusaDocumentoDto();
            dto.setJustificativa("Valor divergente do combinado.");

            DocumentoEntradaRespostaDto resultado = sDocumentoEntrada.recusarDocumento(300L, dto, admin.getEmail());

            assertThat(resultado.getStatusAprovacao()).isEqualTo(EnStatusAprovacaoFinanceira.RECUSADO);
            assertThat(resultado.getJustificativaRecusa()).isEqualTo("Valor divergente do combinado.");
        }

        @Test
        void deveLancarIllegalStateException_QuandoNaoPendente() {
            stubUsuario(admin);
            EDocumentoEntrada documento = new EDocumentoEntrada();
            documento.setId(300L);
            documento.setStatusAprovacao(EnStatusAprovacaoFinanceira.APROVADO);
            when(documentoInterface.findById(300L)).thenReturn(Optional.of(documento));

            assertThatThrownBy(() -> sDocumentoEntrada.recusarDocumento(300L, new RecusaDocumentoDto(), admin.getEmail()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class EditarNfeTests {

        private EDocumentoEntrada documentoNfe() {
            EDocumentoEntrada documento = new EDocumentoEntrada();
            documento.setId(300L);
            documento.setTipoDocumento(EnTipoDocumentoFinanceiro.NF_E);
            documento.setStatusAprovacao(EnStatusAprovacaoFinanceira.APROVADO);
            documento.setItens(List.of());
            return documento;
        }

        @Test
        void deveEditar_QuandoSenhaCorreta() {
            stubUsuario(admin);
            EDocumentoEntrada documento = documentoNfe();
            when(documentoInterface.findById(300L)).thenReturn(Optional.of(documento));
            when(documentoInterface.save(any(EDocumentoEntrada.class))).thenAnswer(inv -> inv.getArgument(0));

            NfeUpdateDto dto = new NfeUpdateDto();
            dto.setNumeroDocumento("99999");

            DocumentoEntradaRespostaDto resultado =
                    sDocumentoEntrada.editarNfe(300L, dto, "senha123", admin.getEmail());

            assertThat(resultado.getNumeroDocumento()).isEqualTo("99999");
            assertThat(resultado.getUltimaEdicaoPorEmail()).isEqualTo(admin.getEmail());
        }

        @Test
        void deveLancarExcecao_QuandoSenhaIncorreta() {
            stubUsuario(admin);

            assertThatThrownBy(() -> sDocumentoEntrada.editarNfe(
                    300L, new NfeUpdateDto(), "senhaErrada", admin.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Senha de confirmação inválida");
            verifyNoInteractions(documentoInterface);
        }

        @Test
        void deveLancarExcecao_QuandoDocumentoNaoENfE() {
            stubUsuario(admin);
            EDocumentoEntrada recibo = documentoNfe();
            recibo.setTipoDocumento(EnTipoDocumentoFinanceiro.RECIBO_SIMPLES);
            when(documentoInterface.findById(300L)).thenReturn(Optional.of(recibo));

            assertThatThrownBy(() -> sDocumentoEntrada.editarNfe(300L, new NfeUpdateDto(), "senha123", admin.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("só se aplica a documentos do tipo NF_E");
        }

        @Test
        void deveLancarExcecao_QuandoItemNaoPertenceAoDocumento() {
            stubUsuario(admin);
            EDocumentoEntrada documento = documentoNfe();
            when(documentoInterface.findById(300L)).thenReturn(Optional.of(documento));

            NfeItemUpdateDto itemDto = new NfeItemUpdateDto();
            itemDto.setItemId(999L);
            NfeUpdateDto dto = new NfeUpdateDto();
            dto.setItens(List.of(itemDto));

            assertThatThrownBy(() -> sDocumentoEntrada.editarNfe(300L, dto, "senha123", admin.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não pertence a este documento");
        }
    }

    @Nested
    class VincularProdutoTests {

        @Test
        void deveVincular_SemGerarLancamento_QuandoDocumentoAindaNaoAprovado() {
            stubUsuario(financeiro);
            EDocumentoEntrada documento = new EDocumentoEntrada();
            documento.setStatusAprovacao(EnStatusAprovacaoFinanceira.PENDENTE_APROVACAO);

            EDocumentoEntradaItem item = new EDocumentoEntradaItem();
            item.setId(1L);
            item.setDocumentoEntrada(documento);
            when(itemInterface.findById(1L)).thenReturn(Optional.of(item));

            EInsumo produto = new EInsumo();
            produto.setId(200L);
            when(insumoInterface.findByIdAndStatus(200L, EnStatus.A)).thenReturn(Optional.of(produto));
            when(itemInterface.save(any(EDocumentoEntradaItem.class))).thenAnswer(inv -> inv.getArgument(0));

            VincularProdutoDto dto = new VincularProdutoDto();
            dto.setProdutoId(200L);

            sDocumentoEntrada.vincularProduto(1L, dto, financeiro.getEmail());

            verifyNoInteractions(lancamentoFinanceiroService);
        }

        @Test
        void deveVincularEGerarLancamento_QuandoDocumentoJaAprovado() {
            stubUsuario(financeiro);
            EDocumentoEntrada documento = new EDocumentoEntrada();
            documento.setStatusAprovacao(EnStatusAprovacaoFinanceira.APROVADO);
            documento.setDataEmissao(LocalDate.of(2026, 3, 1));

            EDocumentoEntradaItem item = new EDocumentoEntradaItem();
            item.setId(1L);
            item.setDocumentoEntrada(documento);
            item.setEntradaEstoqueAplicada(true);
            when(itemInterface.findById(1L)).thenReturn(Optional.of(item));

            EInsumo produto = new EInsumo();
            produto.setId(200L);
            when(insumoInterface.findByIdAndStatus(200L, EnStatus.A)).thenReturn(Optional.of(produto));
            when(itemInterface.save(any(EDocumentoEntradaItem.class))).thenAnswer(inv -> inv.getArgument(0));

            VincularProdutoDto dto = new VincularProdutoDto();
            dto.setProdutoId(200L);

            sDocumentoEntrada.vincularProduto(1L, dto, financeiro.getEmail());

            verify(lancamentoFinanceiroService).registrarOuAtualizarSaidaItemDocumento(item, financeiro.getEmail());
        }

        @Test
        void deveLancarExcecao_QuandoProdutoNaoEncontrado() {
            stubUsuario(financeiro);
            EDocumentoEntradaItem item = new EDocumentoEntradaItem();
            item.setId(1L);
            item.setDocumentoEntrada(new EDocumentoEntrada());
            when(itemInterface.findById(1L)).thenReturn(Optional.of(item));
            when(insumoInterface.findByIdAndStatus(200L, EnStatus.A)).thenReturn(Optional.empty());

            VincularProdutoDto dto = new VincularProdutoDto();
            dto.setProdutoId(200L);

            assertThatThrownBy(() -> sDocumentoEntrada.vincularProduto(1L, dto, financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class LeituraTests {

        @Test
        void buscarPorId_deveRetornarDocumento() {
            stubUsuario(financeiro);
            EDocumentoEntrada documento = new EDocumentoEntrada();
            documento.setId(300L);
            documento.setItens(List.of());
            when(documentoInterface.findById(300L)).thenReturn(Optional.of(documento));

            assertThat(sDocumentoEntrada.buscarPorId(300L, financeiro.getEmail()).getId()).isEqualTo(300L);
        }

        @Test
        void buscarPorId_deveLancarEntityNotFoundException_QuandoNaoExiste() {
            stubUsuario(financeiro);
            when(documentoInterface.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sDocumentoEntrada.buscarPorId(999L, financeiro.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void listarPendentesAprovacao_deveDelegarParaRepositorio() {
            stubUsuario(financeiro);
            when(documentoInterface.findByStatusAprovacaoOrderByDataEntradaDesc(EnStatusAprovacaoFinanceira.PENDENTE_APROVACAO))
                    .thenReturn(List.of());

            assertThat(sDocumentoEntrada.listarPendentesAprovacao(financeiro.getEmail())).isEmpty();
        }
    }
}
