package br.com.gado.services;

import br.com.gado.dto.consumoInsumoDto.SobraAlimentacaoCadastroDto;
import br.com.gado.dto.consumoInsumoDto.SobraAlimentacaoRespostaDto;
import br.com.gado.entities.EConsumoInsumo;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.ESobraAlimentacao;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnStatusSobra;
import br.com.gado.repositories.IConsumoInsumo;
import br.com.gado.repositories.ISobraAlimentacao;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SSobraAlimentacaoTest {

    @InjectMocks
    private SSobraAlimentacao sSobraAlimentacao;

    @Mock
    private ISobraAlimentacao sobraAlimentacaoInterface;

    @Mock
    private IConsumoInsumo consumoInsumoInterface;

    @Mock
    private IUnidadeMedida unidadeMedidaInterface;

    @Mock
    private IUsuario usuarioInterface;

    @Mock
    private SLancamentoFinanceiro lancamentoFinanceiroService;

    private static final Long CONSUMO_ID = 50L;

    private EUsuario autor;
    private EUnidadeMedida primaria;
    private EUnidadeMedida secundaria;
    private EInsumo insumo;
    private EConsumoInsumo consumo;

    @BeforeEach
    void setUp() {
        autor = new EUsuario();
        autor.setEmail("cuidador@fazenda.com");
        autor.setPerfil(EnPerfilUsuario.CUIDADOR);

        primaria = new EUnidadeMedida();
        primaria.setId(1L);
        primaria.setUnidade("SACA");

        secundaria = new EUnidadeMedida();
        secundaria.setId(2L);
        secundaria.setUnidade("KG");

        insumo = new EInsumo();
        insumo.setId(200L);
        insumo.setUnidadeMedidaPrimaria(primaria);
        insumo.setUnidadeMedidaSecundaria(secundaria);
        insumo.setFatorConversao(40.0);

        consumo = new EConsumoInsumo();
        consumo.setId(CONSUMO_ID);
        consumo.setInsumo(insumo);
        consumo.setQuantidadeBaixaUnidadePrimaria(10.0);
        consumo.setRegistradoPorEmail(autor.getEmail());
        consumo.setStatus(EnStatus.A);
    }

    private SobraAlimentacaoCadastroDto dto(double quantidade, Long unidadeMedidaId, boolean reaproveitado) {
        SobraAlimentacaoCadastroDto dto = new SobraAlimentacaoCadastroDto();
        dto.setQuantidade(quantidade);
        dto.setUnidadeMedidaId(unidadeMedidaId);
        dto.setReaproveitado(reaproveitado);
        return dto;
    }

    private void stubUsuarioEConsumo(EUsuario usuario) {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(usuario));
        when(consumoInsumoInterface.findById(CONSUMO_ID)).thenReturn(Optional.of(consumo));
    }

    @Nested
    class RegistrarOuAtualizarSobraTests {

        private void stubSaveIdentidade() {
            when(sobraAlimentacaoInterface.save(any(ESobraAlimentacao.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        void deveClassificarComoOk_QuandoPercentualDentroDaFaixaIdeal() {
            stubUsuarioEConsumo(autor);
            stubSaveIdentidade();
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(CONSUMO_ID)).thenReturn(Optional.empty());

            SobraAlimentacaoRespostaDto resultado =
                    sSobraAlimentacao.registrarOuAtualizarSobra(CONSUMO_ID, dto(0.4, null, true), autor.getEmail());

            assertThat(resultado.getStatusFaixa()).isEqualTo(EnStatusSobra.OK);
            assertThat(resultado.getPercentualSobra()).isEqualTo(4.0);
            assertThat(resultado.getQuantidadeAjusteRecomendada()).isNull();
            verify(lancamentoFinanceiroService).estornarPerdaAlimentacao(any());
            verify(lancamentoFinanceiroService, never()).contabilizarPerdaAlimentacao(any());
        }

        @Test
        void deveClassificarComoAbaixo_QuandoPercentualMenorQue3PorCento() {
            stubUsuarioEConsumo(autor);
            stubSaveIdentidade();
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(CONSUMO_ID)).thenReturn(Optional.empty());

            SobraAlimentacaoRespostaDto resultado =
                    sSobraAlimentacao.registrarOuAtualizarSobra(CONSUMO_ID, dto(0.2, null, true), autor.getEmail());

            assertThat(resultado.getStatusFaixa()).isEqualTo(EnStatusSobra.ABAIXO);
            assertThat(resultado.getQuantidadeAjusteRecomendada()).isEqualTo(0.2, org.assertj.core.data.Offset.offset(1e-9));
        }

        @Test
        void deveClassificarComoAcima_QuandoPercentualMaiorQue5PorCento() {
            stubUsuarioEConsumo(autor);
            stubSaveIdentidade();
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(CONSUMO_ID)).thenReturn(Optional.empty());

            SobraAlimentacaoRespostaDto resultado =
                    sSobraAlimentacao.registrarOuAtualizarSobra(CONSUMO_ID, dto(0.6, null, true), autor.getEmail());

            assertThat(resultado.getStatusFaixa()).isEqualTo(EnStatusSobra.ACIMA);
            assertThat(resultado.getQuantidadeAjusteRecomendada()).isEqualTo(0.2, org.assertj.core.data.Offset.offset(1e-9));
        }

        @Test
        void deveClassificarComoPerda_QuandoNaoReaproveitada() {
            stubUsuarioEConsumo(autor);
            stubSaveIdentidade();
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(CONSUMO_ID)).thenReturn(Optional.empty());

            SobraAlimentacaoRespostaDto resultado =
                    sSobraAlimentacao.registrarOuAtualizarSobra(CONSUMO_ID, dto(0.4, null, false), autor.getEmail());

            assertThat(resultado.getStatusFaixa()).isEqualTo(EnStatusSobra.PERDA);
            verify(lancamentoFinanceiroService).contabilizarPerdaAlimentacao(any());
            verify(lancamentoFinanceiroService, never()).estornarPerdaAlimentacao(any());
        }

        @Test
        void deveConverterQuantidadeDaUnidadeSecundariaParaPrimaria() {
            stubUsuarioEConsumo(autor);
            stubSaveIdentidade();
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(CONSUMO_ID)).thenReturn(Optional.empty());
            when(unidadeMedidaInterface.findById(secundaria.getId())).thenReturn(Optional.of(secundaria));

            SobraAlimentacaoRespostaDto resultado = sSobraAlimentacao.registrarOuAtualizarSobra(
                    CONSUMO_ID, dto(16.0, secundaria.getId(), true), autor.getEmail());

            assertThat(resultado.getQuantidadeSobraUnidadePrimaria()).isEqualTo(0.4);
            assertThat(resultado.getUnidadeRegistroSigla()).isEqualTo("KG");
            assertThat(resultado.getPercentualSobra()).isEqualTo(4.0);
        }

        @Test
        void deveAtualizarSobraExistente_EmVezDeCriarNova() {
            stubUsuarioEConsumo(autor);
            stubSaveIdentidade();
            ESobraAlimentacao existente = new ESobraAlimentacao();
            existente.setId(777L);
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(CONSUMO_ID)).thenReturn(Optional.of(existente));

            sSobraAlimentacao.registrarOuAtualizarSobra(CONSUMO_ID, dto(0.4, null, true), autor.getEmail());

            ArgumentCaptor<ESobraAlimentacao> captor = ArgumentCaptor.forClass(ESobraAlimentacao.class);
            verify(sobraAlimentacaoInterface).save(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(777L);
        }

        @Test
        void deveLancarExcecao_QuandoSobraMaiorQueQuantidadeAplicada() {
            stubUsuarioEConsumo(autor);

            assertThatThrownBy(() -> sSobraAlimentacao.registrarOuAtualizarSobra(
                    CONSUMO_ID, dto(11.0, null, true), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não pode ser maior");
            verify(sobraAlimentacaoInterface, never()).save(any());
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoConsumoNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(autor.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(autor));
            when(consumoInsumoInterface.findById(CONSUMO_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sSobraAlimentacao.registrarOuAtualizarSobra(
                    CONSUMO_ID, dto(0.4, null, true), autor.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioNaoEhAutorNemGerenteNemAdministrador() {
            EUsuario outroUsuario = new EUsuario();
            outroUsuario.setEmail("outro@fazenda.com");
            outroUsuario.setPerfil(EnPerfilUsuario.CUIDADOR);
            stubUsuarioEConsumo(outroUsuario);

            assertThatThrownBy(() -> sSobraAlimentacao.registrarOuAtualizarSobra(
                    CONSUMO_ID, dto(0.4, null, true), outroUsuario.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("só pode registrar sobra");
            verify(sobraAlimentacaoInterface, never()).save(any());
        }

        @Test
        void devePermitirGerente_MesmoNaoSendoAutorDoLancamento() {
            EUsuario gerente = new EUsuario();
            gerente.setEmail("gerente@fazenda.com");
            gerente.setPerfil(EnPerfilUsuario.GERENTE);
            stubUsuarioEConsumo(gerente);
            stubSaveIdentidade();
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(CONSUMO_ID)).thenReturn(Optional.empty());

            SobraAlimentacaoRespostaDto resultado = sSobraAlimentacao.registrarOuAtualizarSobra(
                    CONSUMO_ID, dto(0.4, null, true), gerente.getEmail());

            assertThat(resultado).isNotNull();
        }

        @Test
        void deveLancarExcecao_QuandoUnidadeInformadaNaoPertenceAoInsumo() {
            stubUsuarioEConsumo(autor);
            EUnidadeMedida outraUnidade = new EUnidadeMedida();
            outraUnidade.setId(999L);
            when(unidadeMedidaInterface.findById(999L)).thenReturn(Optional.of(outraUnidade));

            assertThatThrownBy(() -> sSobraAlimentacao.registrarOuAtualizarSobra(
                    CONSUMO_ID, dto(0.4, 999L, true), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não corresponde às unidades cadastradas");
        }

        @Test
        void deveLancarExcecao_QuandoUnidadeInformadaNaoExiste() {
            stubUsuarioEConsumo(autor);
            when(unidadeMedidaInterface.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sSobraAlimentacao.registrarOuAtualizarSobra(
                    CONSUMO_ID, dto(0.4, 999L, true), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não encontrada");
        }

        @Test
        void deveLancarExcecao_QuandoInsumoSemFatorConversao_EUnidadeSecundariaInformada() {
            insumo.setFatorConversao(null);
            stubUsuarioEConsumo(autor);
            when(unidadeMedidaInterface.findById(secundaria.getId())).thenReturn(Optional.of(secundaria));

            assertThatThrownBy(() -> sSobraAlimentacao.registrarOuAtualizarSobra(
                    CONSUMO_ID, dto(1.0, secundaria.getId(), true), autor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("fator de conversão");
        }

        @Test
        void deveLancarExcecao_QuandoEmailDoUsuarioEmBranco() {
            assertThatThrownBy(() -> sSobraAlimentacao.registrarOuAtualizarSobra(
                    CONSUMO_ID, dto(0.4, null, true), " "))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(consumoInsumoInterface);
        }
    }

    @Nested
    class ExcluirSobraTests {

        @Test
        void deveExcluirEEstornarPerda_QuandoSobraNaoEraReaproveitada() {
            stubUsuarioEConsumo(autor);
            ESobraAlimentacao sobra = new ESobraAlimentacao();
            sobra.setId(321L);
            sobra.setReaproveitado(false);
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(CONSUMO_ID)).thenReturn(Optional.of(sobra));

            sSobraAlimentacao.excluirSobra(CONSUMO_ID, autor.getEmail());

            verify(lancamentoFinanceiroService).estornarPerdaAlimentacao(321L);
            verify(sobraAlimentacaoInterface).delete(sobra);
        }

        @Test
        void deveExcluirSemEstornar_QuandoSobraEraReaproveitada() {
            stubUsuarioEConsumo(autor);
            ESobraAlimentacao sobra = new ESobraAlimentacao();
            sobra.setId(321L);
            sobra.setReaproveitado(true);
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(CONSUMO_ID)).thenReturn(Optional.of(sobra));

            sSobraAlimentacao.excluirSobra(CONSUMO_ID, autor.getEmail());

            verify(lancamentoFinanceiroService, never()).estornarPerdaAlimentacao(anyLong());
            verify(sobraAlimentacaoInterface).delete(sobra);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoNenhumaSobraRegistrada() {
            stubUsuarioEConsumo(autor);
            when(sobraAlimentacaoInterface.findByConsumoInsumo_Id(CONSUMO_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sSobraAlimentacao.excluirSobra(CONSUMO_ID, autor.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioSemPermissao() {
            EUsuario outroUsuario = new EUsuario();
            outroUsuario.setEmail("outro@fazenda.com");
            outroUsuario.setPerfil(EnPerfilUsuario.CUIDADOR);
            stubUsuarioEConsumo(outroUsuario);

            assertThatThrownBy(() -> sSobraAlimentacao.excluirSobra(CONSUMO_ID, outroUsuario.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
            verify(sobraAlimentacaoInterface, never()).delete(any());
        }
    }
}
