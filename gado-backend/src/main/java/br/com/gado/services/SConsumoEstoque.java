package br.com.gado.services;

import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueCadastroDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueCancelamentoDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueItemCadastroDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueItemRespostaDto;
import br.com.gado.dto.consumoEstoqueDto.ConsumoEstoqueRespostaDto;
import br.com.gado.entities.EConsumoEstoque;
import br.com.gado.entities.EConsumoEstoqueItem;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IConsumoEstoque;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Serviço da funcionalidade "Consumo de Estoque": baixa de insumos do próprio
 * estoque, registrada por qualquer usuário ativo, com motivo/finalidade
 * obrigatório. O cancelamento (com justificativa obrigatória) é restrito ao
 * autor da movimentação ou a Administradores, e estorna o estoque.
 */
@Service
public class SConsumoEstoque {

    @Autowired
    private IConsumoEstoque consumoEstoqueInterface;

    @Autowired
    private IInsumo insumoInterface;

    @Autowired
    private IUnidadeMedida unidadeMedidaInterface;

    @Autowired
    private IUsuario usuarioInterface;

    // ── Permissões ───────────────────────────────────────────────────────

    /** Qualquer usuário ativo pode registrar consumo do próprio estoque. */
    public void validaUsuarioAtivo(String emailUsuario) {
        resolveUsuarioObrigatorio(emailUsuario);
    }

    private EUsuario resolveUsuarioObrigatorio(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        return usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    /** Apenas quem criou a movimentação ou um Administrador podem cancelá-la. */
    private void validaPermissaoCancelamento(EUsuario usuario, EConsumoEstoque consumo) {
        if (usuario.getPerfil() == EnPerfilUsuario.ADMINISTRADOR) {
            return;
        }
        if (consumo.getCriadoPorEmail() != null
                && consumo.getCriadoPorEmail().trim().equalsIgnoreCase(usuario.getEmail().trim())) {
            return;
        }
        throw new IllegalArgumentException("Você só pode cancelar movimentações que você mesmo criou.");
    }

    // ── Consumo de Estoque ───────────────────────────────────────────────

    @Transactional
    public ConsumoEstoqueRespostaDto registrarConsumo(ConsumoEstoqueCadastroDto dto, String emailUsuario) {
        resolveUsuarioObrigatorio(emailUsuario);

        EConsumoEstoque consumo = new EConsumoEstoque();
        consumo.setMotivo(dto.getMotivo().trim());
        consumo.setDataConsumo(dto.getDataConsumo() != null ? dto.getDataConsumo() : LocalDateTime.now());
        consumo.setCriadoPorEmail(emailUsuario.trim());
        consumo.setCancelado(false);

        List<EConsumoEstoqueItem> itens = new ArrayList<>();
        for (ConsumoEstoqueItemCadastroDto itemDto : dto.getItens()) {
            EInsumo insumo = insumoInterface.findByIdAndStatus(itemDto.getInsumoId(), EnStatus.A)
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado ou inativo."));

            if (insumo.getUnidadeMedidaPrimaria() == null) {
                throw new IllegalArgumentException(String.format(
                        "O produto \"%s\" não possui unidade de medida cadastrada e não pode ter consumo registrado.",
                        insumo.getNome()));
            }

            EUnidadeMedida unidadeRegistro = resolveUnidadeRegistro(insumo, itemDto.getUnidadeMedidaId());
            double quantidadeBaixa = converterParaUnidadePrimaria(insumo, unidadeRegistro, itemDto.getQuantidade());

            double saldoAtual = insumo.getSaldoAtual() != null ? insumo.getSaldoAtual() : 0.0;
            if (saldoAtual < quantidadeBaixa) {
                throw new IllegalArgumentException(String.format(
                        "Estoque insuficiente para o produto \"%s\". Saldo atual: %.2f %s.",
                        insumo.getNome(), saldoAtual, insumo.getUnidadeMedidaPrimaria().getUnidade()));
            }

            insumo.setSaldoAtual(saldoAtual - quantidadeBaixa);
            insumoInterface.save(insumo);

            EConsumoEstoqueItem item = new EConsumoEstoqueItem();
            item.setConsumoEstoque(consumo);
            item.setInsumo(insumo);
            item.setQuantidadeRegistrada(itemDto.getQuantidade());
            item.setUnidadeRegistro(unidadeRegistro);
            item.setQuantidadeBaixaUnidadePrimaria(quantidadeBaixa);
            itens.add(item);
        }

        consumo.setItens(itens);
        EConsumoEstoque salvo = consumoEstoqueInterface.save(consumo);
        return toRespostaDto(salvo);
    }

    @Transactional
    public ConsumoEstoqueRespostaDto cancelarConsumo(Long id, ConsumoEstoqueCancelamentoDto dto, String emailUsuario) {
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);

        EConsumoEstoque consumo = consumoEstoqueInterface.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimentação não encontrada."));

        if (Boolean.TRUE.equals(consumo.getCancelado())) {
            throw new IllegalArgumentException("Esta movimentação já foi cancelada.");
        }

        validaPermissaoCancelamento(usuario, consumo);

        for (EConsumoEstoqueItem item : consumo.getItens()) {
            EInsumo insumo = item.getInsumo();
            double saldoAtual = insumo.getSaldoAtual() != null ? insumo.getSaldoAtual() : 0.0;
            insumo.setSaldoAtual(saldoAtual + item.getQuantidadeBaixaUnidadePrimaria());
            insumoInterface.save(insumo);
        }

        consumo.setCancelado(true);
        consumo.setMotivoCancelamento(dto.getMotivoCancelamento().trim());
        consumo.setCanceladoPorEmail(emailUsuario.trim());
        consumo.setCanceladoEm(LocalDateTime.now());

        EConsumoEstoque salvo = consumoEstoqueInterface.save(consumo);
        return toRespostaDto(salvo);
    }

    @Transactional
    public List<ConsumoEstoqueRespostaDto> listarTodos() {
        return consumoEstoqueInterface.findAllByOrderByDataConsumoDesc()
                .stream()
                .map(this::toRespostaDto)
                .collect(Collectors.toList());
    }

    // ── Helpers de conversão ────────────────────────────────────────────

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
                    "A unidade informada não corresponde às unidades cadastradas para este produto.");
        }

        return unidade;
    }

    /**
     * Converte a quantidade informada para a unidade primária (a mesma em que saldoAtual é controlado).
     * Se a unidade de registro for a secundária, divide pelo fator de conversão.
     */
    private double converterParaUnidadePrimaria(EInsumo insumo, EUnidadeMedida unidadeRegistro, double quantidade) {
        boolean ehPrimaria = Objects.equals(unidadeRegistro.getId(), insumo.getUnidadeMedidaPrimaria().getId());
        if (ehPrimaria) {
            return quantidade;
        }

        if (insumo.getFatorConversao() == null || insumo.getFatorConversao() <= 0) {
            throw new IllegalArgumentException(
                    "Este produto não possui fator de conversão cadastrado para a unidade secundária.");
        }

        return quantidade / insumo.getFatorConversao();
    }

    private ConsumoEstoqueRespostaDto toRespostaDto(EConsumoEstoque consumo) {
        ConsumoEstoqueRespostaDto dto = new ConsumoEstoqueRespostaDto();
        dto.setId(consumo.getId());
        dto.setMotivo(consumo.getMotivo());
        dto.setDataConsumo(consumo.getDataConsumo());
        dto.setCriadoPorEmail(consumo.getCriadoPorEmail());
        dto.setCancelado(consumo.getCancelado());
        dto.setMotivoCancelamento(consumo.getMotivoCancelamento());
        dto.setCanceladoPorEmail(consumo.getCanceladoPorEmail());
        dto.setCanceladoEm(consumo.getCanceladoEm());

        if (consumo.getCriadoPorEmail() != null) {
            usuarioInterface.findByEmailAndStatus(consumo.getCriadoPorEmail(), EnStatus.A)
                    .ifPresent(u -> dto.setCriadoPorNome(u.getNome()));
        }
        if (consumo.getCanceladoPorEmail() != null) {
            usuarioInterface.findByEmailAndStatus(consumo.getCanceladoPorEmail(), EnStatus.A)
                    .ifPresent(u -> dto.setCanceladoPorNome(u.getNome()));
        }

        dto.setItens(consumo.getItens().stream().map(this::toItemRespostaDto).collect(Collectors.toList()));
        return dto;
    }

    private ConsumoEstoqueItemRespostaDto toItemRespostaDto(EConsumoEstoqueItem item) {
        ConsumoEstoqueItemRespostaDto dto = new ConsumoEstoqueItemRespostaDto();
        dto.setId(item.getId());
        dto.setInsumoId(item.getInsumo().getId());
        dto.setInsumoNome(item.getInsumo().getNome());
        dto.setQuantidadeRegistrada(item.getQuantidadeRegistrada());
        dto.setUnidadeRegistroSigla(item.getUnidadeRegistro().getUnidade());
        dto.setQuantidadeBaixaUnidadePrimaria(item.getQuantidadeBaixaUnidadePrimaria());
        if (item.getInsumo().getUnidadeMedidaPrimaria() != null) {
            dto.setUnidadeMedidaPrimariaSigla(item.getInsumo().getUnidadeMedidaPrimaria().getUnidade());
        }
        dto.setSaldoAtualAposConsumo(item.getInsumo().getSaldoAtual());
        return dto;
    }
}
