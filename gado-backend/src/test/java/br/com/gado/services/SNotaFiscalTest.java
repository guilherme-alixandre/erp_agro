package br.com.gado.services;

import br.com.gado.dto.notaFiscalDto.NotaFiscalResumoDto;
import br.com.gado.entities.EDocumentoEntrada;
import br.com.gado.entities.EDocumentoSaida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnStatusAprovacaoFinanceira;
import br.com.gado.enums.EnTipoDocumentoFinanceiro;
import br.com.gado.enums.EnTipoDocumentoSaida;
import br.com.gado.repositories.IDocumentoEntrada;
import br.com.gado.repositories.IDocumentoSaida;
import br.com.gado.repositories.IUsuario;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SNotaFiscalTest {

    @InjectMocks
    private SNotaFiscal sNotaFiscal;

    @Mock private IDocumentoEntrada documentoEntradaInterface;
    @Mock private IDocumentoSaida documentoSaidaInterface;
    @Mock private IUsuario usuarioInterface;

    private EUsuario financeiro;

    @BeforeEach
    void setUp() {
        financeiro = new EUsuario();
        financeiro.setEmail("financeiro@fazenda.com");
        financeiro.setPerfil(EnPerfilUsuario.FINANCEIRO);
    }

    private void stubFinanceiro() {
        when(usuarioInterface.findByEmailAndStatus(financeiro.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(financeiro));
    }

    private EDocumentoEntrada entrada(String numero, LocalDate dataEmissao) {
        EDocumentoEntrada documento = new EDocumentoEntrada();
        documento.setId(1L);
        documento.setTipoDocumento(EnTipoDocumentoFinanceiro.NF_E);
        documento.setStatusAprovacao(EnStatusAprovacaoFinanceira.APROVADO);
        documento.setNumeroDocumento(numero);
        documento.setChaveAcessoNfe("11111111111111111111111111111111111111111111");
        documento.setDataEmissao(dataEmissao);
        documento.setValorTotal(new BigDecimal("100.00"));
        return documento;
    }

    private EDocumentoSaida saida(String numero, LocalDate dataEmissao) {
        EDocumentoSaida documento = new EDocumentoSaida();
        documento.setId(2L);
        documento.setTipoDocumento(EnTipoDocumentoSaida.VENDA_LEITE);
        documento.setNumeroDocumento(numero);
        documento.setChaveAcesso("22222222222222222222222222222222222222222222");
        documento.setDataEmissao(dataEmissao);
        documento.setValorTotal(new BigDecimal("200.00"));
        documento.setItensLeite(List.of());
        documento.setItensAnimal(List.of());
        return documento;
    }

    @Test
    void deveListarEntradasESaidas_OrdenadasPorDataDesc() {
        stubFinanceiro();
        when(documentoEntradaInterface.findByStatusOrderByDataEntradaDesc(EnStatus.A))
                .thenReturn(List.of(entrada("100", LocalDate.of(2026, 1, 1))));
        when(documentoSaidaInterface.findByStatusOrderByDataEmissaoDesc(EnStatus.A))
                .thenReturn(List.of(saida("200", LocalDate.of(2026, 3, 1))));

        List<NotaFiscalResumoDto> resultado = sNotaFiscal.listar(null, null, null, financeiro.getEmail());

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNumeroDocumento()).isEqualTo("200");
        assertThat(resultado.get(0).getDirecao()).isEqualTo("RECEITA");
        assertThat(resultado.get(1).getDirecao()).isEqualTo("ENTRADA");
    }

    @Test
    void deveFiltrarApenasEntradas_QuandoDirecaoEntrada() {
        stubFinanceiro();
        when(documentoEntradaInterface.findByStatusOrderByDataEntradaDesc(EnStatus.A))
                .thenReturn(List.of(entrada("100", LocalDate.of(2026, 1, 1))));

        List<NotaFiscalResumoDto> resultado = sNotaFiscal.listar(null, null, "ENTRADA", financeiro.getEmail());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getDirecao()).isEqualTo("ENTRADA");
    }

    @Test
    void deveFiltrarApenasSaidas_QuandoDirecaoReceita() {
        stubFinanceiro();
        when(documentoSaidaInterface.findByStatusOrderByDataEmissaoDesc(EnStatus.A))
                .thenReturn(List.of(saida("200", LocalDate.of(2026, 3, 1))));

        List<NotaFiscalResumoDto> resultado = sNotaFiscal.listar(null, null, "RECEITA", financeiro.getEmail());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getDirecao()).isEqualTo("RECEITA");
    }

    @Test
    void deveFiltrarPorNumeroDoDocumento() {
        stubFinanceiro();
        when(documentoEntradaInterface.findByStatusOrderByDataEntradaDesc(EnStatus.A))
                .thenReturn(List.of(entrada("100", LocalDate.of(2026, 1, 1)), entrada("999", LocalDate.of(2026, 1, 2))));
        when(documentoSaidaInterface.findByStatusOrderByDataEmissaoDesc(EnStatus.A)).thenReturn(List.of());

        List<NotaFiscalResumoDto> resultado = sNotaFiscal.listar("100", null, null, financeiro.getEmail());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNumeroDocumento()).isEqualTo("100");
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioSemPermissao() {
        EUsuario cuidador = new EUsuario();
        cuidador.setEmail("cuidador@fazenda.com");
        cuidador.setPerfil(EnPerfilUsuario.CUIDADOR);
        when(usuarioInterface.findByEmailAndStatus(cuidador.getEmail(), EnStatus.A)).thenReturn(Optional.of(cuidador));

        assertThatThrownBy(() -> sNotaFiscal.listar(null, null, null, cuidador.getEmail()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
