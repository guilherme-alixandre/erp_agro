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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Sobra de uma alimentação já registrada em "Alimentar Setores": quanto sobrou antes da próxima
 * alimentação e se foi reaproveitada. Reaproveitada dentro de 3%-5% do total aplicado está dentro
 * do ideal; fora da faixa, recomenda ajustar a próxima alimentação (mirando o meio da faixa, 4%).
 * NÃO reaproveitada é registrada como perda real — ver SLancamentoFinanceiro.contabilizarPerdaAlimentacao
 * e SLote.calcularPerdaAlimentacaoAcumulada.
 */
@Service
public class SSobraAlimentacao {

    private static final double PERCENTUAL_ALVO = 0.04;
    private static final double PERCENTUAL_MINIMO = 0.03;
    private static final double PERCENTUAL_MAXIMO = 0.05;

    @Autowired
    private ISobraAlimentacao sobraAlimentacaoInterface;

    @Autowired
    private IConsumoInsumo consumoInsumoInterface;

    @Autowired
    private IUnidadeMedida unidadeMedidaInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private SLancamentoFinanceiro lancamentoFinanceiroService;

    @Transactional
    public SobraAlimentacaoRespostaDto registrarOuAtualizarSobra(Long consumoInsumoId,
                                                                   SobraAlimentacaoCadastroDto dto,
                                                                   String emailUsuario) {
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);
        EConsumoInsumo consumo = consumoInsumoInterface.findById(consumoInsumoId)
                .filter(c -> c.getStatus() == EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento de consumo não encontrado."));

        validaPermissao(usuario, consumo);

        EInsumo insumo = consumo.getInsumo();
        EUnidadeMedida unidadeRegistro = resolveUnidadeRegistro(insumo, dto.getUnidadeMedidaId());
        double quantidadeSobraPrimaria = converterParaUnidadePrimaria(insumo, unidadeRegistro, dto.getQuantidade());

        if (quantidadeSobraPrimaria > consumo.getQuantidadeBaixaUnidadePrimaria()) {
            throw new IllegalArgumentException(
                    "A sobra não pode ser maior que a quantidade aplicada nesta alimentação.");
        }

        double percentual = consumo.getQuantidadeBaixaUnidadePrimaria() > 0
                ? (quantidadeSobraPrimaria / consumo.getQuantidadeBaixaUnidadePrimaria()) * 100
                : 0;

        ESobraAlimentacao sobra = sobraAlimentacaoInterface.findByConsumoInsumo_Id(consumoInsumoId)
                .orElseGet(ESobraAlimentacao::new);
        sobra.setConsumoInsumo(consumo);
        sobra.setQuantidadeSobraRegistrada(dto.getQuantidade());
        sobra.setUnidadeRegistro(unidadeRegistro);
        sobra.setQuantidadeSobraUnidadePrimaria(quantidadeSobraPrimaria);
        sobra.setPercentualSobra(percentual);
        sobra.setReaproveitado(dto.getReaproveitado());
        sobra.setDataRegistro(LocalDateTime.now());
        sobra.setRegistradoPorEmail(emailUsuario != null ? emailUsuario.trim() : null);

        String siglaPrimaria = insumo.getUnidadeMedidaPrimaria() != null
                ? insumo.getUnidadeMedidaPrimaria().getUnidade() : "";
        aplicarCalculoFaixa(sobra, consumo.getQuantidadeBaixaUnidadePrimaria(), siglaPrimaria);

        ESobraAlimentacao salva = sobraAlimentacaoInterface.save(sobra);

        if (Boolean.TRUE.equals(dto.getReaproveitado())) {
            lancamentoFinanceiroService.estornarPerdaAlimentacao(salva.getId());
        } else {
            lancamentoFinanceiroService.contabilizarPerdaAlimentacao(salva);
        }

        return toRespostaDto(salva);
    }

    @Transactional
    public void excluirSobra(Long consumoInsumoId, String emailUsuario) {
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);
        EConsumoInsumo consumo = consumoInsumoInterface.findById(consumoInsumoId)
                .filter(c -> c.getStatus() == EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento de consumo não encontrado."));

        validaPermissao(usuario, consumo);

        ESobraAlimentacao sobra = sobraAlimentacaoInterface.findByConsumoInsumo_Id(consumoInsumoId)
                .orElseThrow(() -> new EntityNotFoundException("Nenhuma sobra registrada para esta alimentação."));

        if (Boolean.FALSE.equals(sobra.getReaproveitado())) {
            lancamentoFinanceiroService.estornarPerdaAlimentacao(sobra.getId());
        }
        sobraAlimentacaoInterface.delete(sobra);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private EUsuario resolveUsuarioObrigatorio(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        return usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    /** Autor do lançamento de consumo, Gerente ou Administrador podem registrar/editar/excluir a sobra. */
    private void validaPermissao(EUsuario usuario, EConsumoInsumo consumo) {
        if (usuario.getPerfil() == EnPerfilUsuario.ADMINISTRADOR || usuario.getPerfil() == EnPerfilUsuario.GERENTE) {
            return;
        }
        if (consumo.getRegistradoPorEmail() != null
                && consumo.getRegistradoPorEmail().trim().equalsIgnoreCase(usuario.getEmail().trim())) {
            return;
        }
        throw new IllegalArgumentException("Você só pode registrar sobra em lançamentos que você mesmo criou.");
    }

    private EUnidadeMedida resolveUnidadeRegistro(EInsumo insumo, Long unidadeMedidaId) {
        if (unidadeMedidaId == null) {
            return insumo.getUnidadeMedidaPrimaria();
        }

        EUnidadeMedida unidade = unidadeMedidaInterface.findById(unidadeMedidaId)
                .orElseThrow(() -> new IllegalArgumentException("Unidade de medida informada não encontrada."));

        boolean ehPrimaria = Objects.equals(unidade.getId(), insumo.getUnidadeMedidaPrimaria().getId());
        boolean ehSecundaria = insumo.getUnidadeMedidaSecundaria() != null
                && Objects.equals(unidade.getId(), insumo.getUnidadeMedidaSecundaria().getId());

        if (!ehPrimaria && !ehSecundaria) {
            throw new IllegalArgumentException(
                    "A unidade informada não corresponde às unidades cadastradas para este insumo.");
        }

        return unidade;
    }

    private double converterParaUnidadePrimaria(EInsumo insumo, EUnidadeMedida unidadeRegistro, double quantidade) {
        boolean ehPrimaria = Objects.equals(unidadeRegistro.getId(), insumo.getUnidadeMedidaPrimaria().getId());
        if (ehPrimaria) {
            return quantidade;
        }

        if (insumo.getFatorConversao() == null || insumo.getFatorConversao() <= 0) {
            throw new IllegalArgumentException(
                    "Este insumo não possui fator de conversão cadastrado para a unidade secundária.");
        }

        return quantidade / insumo.getFatorConversao();
    }

    /** Preenche statusFaixa/quantidadeAjusteRecomendada/mensagem — fonte única usada pela resposta e pela listagem. */
    private void aplicarCalculoFaixa(ESobraAlimentacao sobra, double quantidadeAplicada, String siglaPrimaria) {
        double percentual = sobra.getPercentualSobra();
        double metaQuantidade = PERCENTUAL_ALVO * quantidadeAplicada;

        if (Boolean.FALSE.equals(sobra.getReaproveitado())) {
            sobra.setStatusFaixa(EnStatusSobra.PERDA);
            sobra.setQuantidadeAjusteRecomendada(null);
            sobra.setMensagem(String.format(
                    "Sobra não reaproveitada — registrada como perda de %.2f %s (%.1f%% do total aplicado).",
                    sobra.getQuantidadeSobraUnidadePrimaria(), siglaPrimaria, percentual));
        } else if (percentual < PERCENTUAL_MINIMO * 100) {
            double ajuste = metaQuantidade - sobra.getQuantidadeSobraUnidadePrimaria();
            sobra.setStatusFaixa(EnStatusSobra.ABAIXO);
            sobra.setQuantidadeAjusteRecomendada(ajuste);
            sobra.setMensagem(String.format(
                    "Sobra abaixo do ideal (%.1f%%). Recomenda-se aumentar aproximadamente %.2f %s na próxima alimentação.",
                    percentual, ajuste, siglaPrimaria));
        } else if (percentual > PERCENTUAL_MAXIMO * 100) {
            double ajuste = sobra.getQuantidadeSobraUnidadePrimaria() - metaQuantidade;
            sobra.setStatusFaixa(EnStatusSobra.ACIMA);
            sobra.setQuantidadeAjusteRecomendada(ajuste);
            sobra.setMensagem(String.format(
                    "Sobra acima do ideal (%.1f%%). Recomenda-se reduzir aproximadamente %.2f %s na próxima alimentação.",
                    percentual, ajuste, siglaPrimaria));
        } else {
            sobra.setStatusFaixa(EnStatusSobra.OK);
            sobra.setQuantidadeAjusteRecomendada(null);
            sobra.setMensagem(String.format("Sobra dentro da faixa ideal (%.1f%%).", percentual));
        }
    }

    private SobraAlimentacaoRespostaDto toRespostaDto(ESobraAlimentacao sobra) {
        SobraAlimentacaoRespostaDto dto = new SobraAlimentacaoRespostaDto();
        dto.setConsumoInsumoId(sobra.getConsumoInsumo().getId());
        dto.setQuantidadeSobraRegistrada(sobra.getQuantidadeSobraRegistrada());
        dto.setUnidadeRegistroSigla(sobra.getUnidadeRegistro().getUnidade());
        dto.setQuantidadeSobraUnidadePrimaria(sobra.getQuantidadeSobraUnidadePrimaria());

        EInsumo insumo = sobra.getConsumoInsumo().getInsumo();
        dto.setUnidadeMedidaPrimariaSigla(insumo.getUnidadeMedidaPrimaria() != null
                ? insumo.getUnidadeMedidaPrimaria().getUnidade() : "");

        dto.setPercentualSobra(sobra.getPercentualSobra());
        dto.setReaproveitado(sobra.getReaproveitado());
        dto.setStatusFaixa(sobra.getStatusFaixa());
        dto.setQuantidadeAjusteRecomendada(sobra.getQuantidadeAjusteRecomendada());
        dto.setMensagem(sobra.getMensagem());

        return dto;
    }
}
