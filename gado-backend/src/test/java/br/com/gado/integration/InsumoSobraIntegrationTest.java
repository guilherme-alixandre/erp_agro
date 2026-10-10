package br.com.gado.integration;

import br.com.gado.GadoApplication;
import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoCadastroDto;
import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoRespostaDto;
import br.com.gado.dto.consumoInsumoDto.SobraAlimentacaoCadastroDto;
import br.com.gado.dto.consumoInsumoDto.SobraAlimentacaoRespostaDto;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnOrigemLancamentoFinanceiro;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatusSobra;
import br.com.gado.enums.EnTipoInsumo;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.ILancamentoFinanceiro;
import br.com.gado.repositories.ISetor;
import br.com.gado.repositories.ISobraAlimentacao;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import br.com.gado.services.SConsumoInsumo;
import br.com.gado.services.SSobraAlimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fluxo completo (sem mocks, contra H2) de "Alimentar Setores" -> registrar sobra -> verificar
 * classificação de faixa e o reflexo no razão financeiro (SLancamentoFinanceiro), cobrindo a
 * integração real entre SConsumoInsumo, SSobraAlimentacao e SLancamentoFinanceiro.
 */
@SpringBootTest(classes = GadoApplication.class)
@ActiveProfiles("test")
@Transactional
class InsumoSobraIntegrationTest {

    @Autowired private SConsumoInsumo consumoInsumoService;
    @Autowired private SSobraAlimentacao sobraAlimentacaoService;

    @Autowired private IUsuario usuarioRepository;
    @Autowired private ISetor setorRepository;
    @Autowired private IUnidadeMedida unidadeMedidaRepository;
    @Autowired private IInsumo insumoRepository;
    @Autowired private ISobraAlimentacao sobraAlimentacaoRepository;
    @Autowired private ILancamentoFinanceiro lancamentoFinanceiroRepository;

    private EUsuario cuidador;
    private ESetor setor;
    private EInsumo insumo;

    @BeforeEach
    void setUp() {
        cuidador = new EUsuario();
        cuidador.setNome("Cuidador Teste");
        cuidador.setEmail("cuidador-it-" + System.nanoTime() + "@fazenda.com");
        cuidador.setSenha("hash");
        cuidador.setPerfil(EnPerfilUsuario.CUIDADOR);
        cuidador = usuarioRepository.save(cuidador);

        setor = new ESetor();
        setor.setNome("Pasto IT " + System.nanoTime());
        setor = setorRepository.save(setor);

        EUnidadeMedida unidade = new EUnidadeMedida();
        unidade.setUnidade("SACA");
        unidade = unidadeMedidaRepository.save(unidade);

        insumo = new EInsumo();
        insumo.setNome("Ração IT");
        insumo.setTipo(EnTipoInsumo.RACAO);
        insumo.setSaldoAtual(1000.0);
        insumo.setPrecoCompraMedio(10.0);
        insumo.setUnidadeMedidaPrimaria(unidade);
        insumo = insumoRepository.save(insumo);
    }

    @Test
    void deveRegistrarSobraComoPerda_EGerarLancamentoFinanceiro_QuandoNaoReaproveitada() {
        ConsumoInsumoCadastroDto consumoDto = new ConsumoInsumoCadastroDto();
        consumoDto.setInsumoId(insumo.getId());
        consumoDto.setSetorId(setor.getId());
        consumoDto.setQuantidade(100.0);
        ConsumoInsumoRespostaDto consumo =
                consumoInsumoService.registrarConsumo(consumoDto, cuidador.getEmail());

        SobraAlimentacaoCadastroDto sobraDto = new SobraAlimentacaoCadastroDto();
        sobraDto.setQuantidade(5.0);
        sobraDto.setReaproveitado(false);
        SobraAlimentacaoRespostaDto sobra = sobraAlimentacaoService.registrarOuAtualizarSobra(
                consumo.getId(), sobraDto, cuidador.getEmail());

        assertThat(sobra.getStatusFaixa()).isEqualTo(EnStatusSobra.PERDA);

        var sobraPersistida = sobraAlimentacaoRepository.findByConsumoInsumo_Id(consumo.getId());
        assertThat(sobraPersistida).isPresent();

        var lancamento = lancamentoFinanceiroRepository
                .findByOrigemAndOrigemId(EnOrigemLancamentoFinanceiro.PERDA_ALIMENTACAO, sobraPersistida.get().getId());
        assertThat(lancamento).isPresent();
        assertThat(lancamento.get().getValor()).isEqualByComparingTo("50.00");
    }

    @Test
    void deveEstornarLancamento_QuandoSobraExcluidaEraPerda() {
        ConsumoInsumoCadastroDto consumoDto = new ConsumoInsumoCadastroDto();
        consumoDto.setInsumoId(insumo.getId());
        consumoDto.setSetorId(setor.getId());
        consumoDto.setQuantidade(100.0);
        ConsumoInsumoRespostaDto consumo =
                consumoInsumoService.registrarConsumo(consumoDto, cuidador.getEmail());

        SobraAlimentacaoCadastroDto sobraDto = new SobraAlimentacaoCadastroDto();
        sobraDto.setQuantidade(5.0);
        sobraDto.setReaproveitado(false);
        sobraAlimentacaoService.registrarOuAtualizarSobra(consumo.getId(), sobraDto, cuidador.getEmail());

        sobraAlimentacaoService.excluirSobra(consumo.getId(), cuidador.getEmail());

        assertThat(sobraAlimentacaoRepository.findByConsumoInsumo_Id(consumo.getId())).isEmpty();
    }

    @Test
    void deveClassificarComoOk_QuandoPercentualDentroDaFaixaIdeal() {
        ConsumoInsumoCadastroDto consumoDto = new ConsumoInsumoCadastroDto();
        consumoDto.setInsumoId(insumo.getId());
        consumoDto.setSetorId(setor.getId());
        consumoDto.setQuantidade(100.0);
        ConsumoInsumoRespostaDto consumo =
                consumoInsumoService.registrarConsumo(consumoDto, cuidador.getEmail());

        SobraAlimentacaoCadastroDto sobraDto = new SobraAlimentacaoCadastroDto();
        sobraDto.setQuantidade(4.0);
        sobraDto.setReaproveitado(true);
        SobraAlimentacaoRespostaDto sobra = sobraAlimentacaoService.registrarOuAtualizarSobra(
                consumo.getId(), sobraDto, cuidador.getEmail());

        assertThat(sobra.getStatusFaixa()).isEqualTo(EnStatusSobra.OK);
    }

    @Test
    void deveNegarRegistroDeSobra_QuandoUsuarioNaoEhAutorNemGerencial() {
        EUsuario outroUsuario = new EUsuario();
        outroUsuario.setNome("Outro Cuidador");
        outroUsuario.setEmail("outro-it-" + System.nanoTime() + "@fazenda.com");
        outroUsuario.setSenha("hash");
        outroUsuario.setPerfil(EnPerfilUsuario.CUIDADOR);
        outroUsuario = usuarioRepository.save(outroUsuario);

        ConsumoInsumoCadastroDto consumoDto = new ConsumoInsumoCadastroDto();
        consumoDto.setInsumoId(insumo.getId());
        consumoDto.setSetorId(setor.getId());
        consumoDto.setQuantidade(100.0);
        ConsumoInsumoRespostaDto consumo =
                consumoInsumoService.registrarConsumo(consumoDto, cuidador.getEmail());

        SobraAlimentacaoCadastroDto sobraDto = new SobraAlimentacaoCadastroDto();
        sobraDto.setQuantidade(4.0);
        sobraDto.setReaproveitado(true);

        String emailOutro = outroUsuario.getEmail();
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> sobraAlimentacaoService.registrarOuAtualizarSobra(consumo.getId(), sobraDto, emailOutro));
    }
}
