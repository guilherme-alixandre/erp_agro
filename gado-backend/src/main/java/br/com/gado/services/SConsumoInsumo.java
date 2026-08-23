package br.com.gado.services;

import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoCadastroDto;
import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoEdicaoDto;
import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoResumoItemDto;
import br.com.gado.dto.consumoInsumoDto.ConsumoInsumoRespostaDto;
import br.com.gado.entities.EConsumoInsumo;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.ELoteSetor;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoMovimentacaoEstoque;
import br.com.gado.repositories.IConsumoInsumo;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.ILoteSetor;
import br.com.gado.repositories.ISetor;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Serviço da funcionalidade "Alimentar Lote": registro de consumo diário de
 * insumos por Setor, com baixa automática de estoque e rateio do consumo
 * entre os animais alocados naquele setor.
 */
@Slf4j
@Service
public class SConsumoInsumo {

    @Autowired
    private IConsumoInsumo consumoInsumoInterface;

    @Autowired
    private IInsumo insumoInterface;

    @Autowired
    private ISetor setorInterface;

    @Autowired
    private IUnidadeMedida unidadeMedidaInterface;

    @Autowired
    private ILoteSetor loteSetorInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private SInsumo insumoService;

    // ── Permissões ───────────────────────────────────────────────────────

    /** Qualquer usuário ativo (incluindo Cuidadores comuns) pode registrar consumo. */
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

    // ── Alimentar Lote ───────────────────────────────────────────────────

    @Transactional
    public ConsumoInsumoRespostaDto registrarConsumo(ConsumoInsumoCadastroDto dto, String emailUsuario) {
        EInsumo insumo = insumoInterface.findByIdAndStatus(dto.getInsumoId(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Insumo não encontrado ou inativo."));

        ESetor setor = setorInterface.findByIdAndStatus(dto.getSetorId(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado ou inativo."));

        if (insumo.getUnidadeMedidaPrimaria() == null) {
            throw new IllegalArgumentException(
                    "Este insumo não possui unidade de medida cadastrada e não pode ter consumo registrado.");
        }

        validaDataNaoFutura(dto.getDataConsumo());

        EUnidadeMedida unidadeRegistro = resolveUnidadeRegistro(insumo, dto.getUnidadeMedidaId());
        double quantidadeBaixa = converterParaUnidadePrimaria(insumo, unidadeRegistro, dto.getQuantidade());

        double saldoAtual = insumo.getSaldoAtual() != null ? insumo.getSaldoAtual() : 0.0;
        if (saldoAtual < quantidadeBaixa) {
            throw new IllegalArgumentException(String.format(
                    "Estoque insuficiente para registrar este consumo. Saldo atual: %.2f %s.",
                    saldoAtual, insumo.getUnidadeMedidaPrimaria().getUnidade()));
        }

        // Baixa de estoque, respeitando o fator de conversão (rule 4)
        insumo.setSaldoAtual(saldoAtual - quantidadeBaixa);
        insumoInterface.save(insumo);
        insumoService.registrarMovimentacao(insumo, EnTipoMovimentacaoEstoque.SAIDA, quantidadeBaixa,
                insumo.getPrecoCompraMedio(),
                dto.getDataConsumo() != null ? dto.getDataConsumo() : LocalDateTime.now(),
                null, setor, null);

        // Rateio: total de animais atualmente alocados neste setor, em lotes ativos (rule 3)
        int totalAnimais = contarAnimaisDoSetor(setor.getId());
        Double consumoPorAnimal = totalAnimais > 0 ? dto.getQuantidade() / totalAnimais : null;
        if (totalAnimais == 0) {
            log.warn("Consumo registrado para o setor {} sem nenhum animal alocado.", setor.getId());
        }

        EConsumoInsumo consumo = new EConsumoInsumo();
        consumo.setInsumo(insumo);
        consumo.setSetor(setor);
        consumo.setQuantidadeRegistrada(dto.getQuantidade());
        consumo.setUnidadeRegistro(unidadeRegistro);
        consumo.setQuantidadeBaixaUnidadePrimaria(quantidadeBaixa);
        consumo.setTotalAnimaisSetor(totalAnimais);
        consumo.setConsumoPorAnimal(consumoPorAnimal);
        consumo.setDataConsumo(dto.getDataConsumo() != null ? dto.getDataConsumo() : LocalDateTime.now());
        consumo.setRegistradoPorEmail(emailUsuario != null ? emailUsuario.trim() : null);

        EConsumoInsumo salvo = consumoInsumoInterface.save(consumo);
        return toRespostaDto(salvo);
    }

    /** Autor do lançamento, Gerente ou Administrador podem editar. */
    @Transactional
    public ConsumoInsumoRespostaDto editarConsumo(Long id, ConsumoInsumoEdicaoDto dto, String emailUsuario) {
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);
        EConsumoInsumo consumo = consumoInsumoInterface.findById(id)
                .filter(c -> c.getStatus() == EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento de consumo não encontrado."));

        validaPermissaoEdicao(usuario, consumo);
        validaDataNaoFutura(dto.getDataConsumo());

        EInsumo insumo = consumo.getInsumo();

        // Estorna a baixa anterior antes de recalcular com os novos valores.
        double saldoComEstorno = (insumo.getSaldoAtual() != null ? insumo.getSaldoAtual() : 0.0)
                + consumo.getQuantidadeBaixaUnidadePrimaria();

        EUnidadeMedida unidadeRegistro = resolveUnidadeRegistro(insumo, dto.getUnidadeMedidaId());
        double novaQuantidadeBaixa = converterParaUnidadePrimaria(insumo, unidadeRegistro, dto.getQuantidade());

        if (saldoComEstorno < novaQuantidadeBaixa) {
            throw new IllegalArgumentException(String.format(
                    "Estoque insuficiente para esta edição. Saldo disponível: %.2f %s.",
                    saldoComEstorno, insumo.getUnidadeMedidaPrimaria().getUnidade()));
        }

        insumo.setSaldoAtual(saldoComEstorno - novaQuantidadeBaixa);
        insumoInterface.save(insumo);
        insumoService.registrarMovimentacao(insumo, EnTipoMovimentacaoEstoque.ENTRADA,
                consumo.getQuantidadeBaixaUnidadePrimaria(), insumo.getPrecoCompraMedio(),
                LocalDateTime.now(), null, consumo.getSetor(), null);
        insumoService.registrarMovimentacao(insumo, EnTipoMovimentacaoEstoque.SAIDA, novaQuantidadeBaixa,
                insumo.getPrecoCompraMedio(),
                dto.getDataConsumo() != null ? dto.getDataConsumo() : LocalDateTime.now(),
                null, consumo.getSetor(), null);

        int totalAnimais = contarAnimaisDoSetor(consumo.getSetor().getId());
        Double consumoPorAnimal = totalAnimais > 0 ? dto.getQuantidade() / totalAnimais : null;

        consumo.setQuantidadeRegistrada(dto.getQuantidade());
        consumo.setUnidadeRegistro(unidadeRegistro);
        consumo.setQuantidadeBaixaUnidadePrimaria(novaQuantidadeBaixa);
        consumo.setTotalAnimaisSetor(totalAnimais);
        consumo.setConsumoPorAnimal(consumoPorAnimal);
        consumo.setDataConsumo(dto.getDataConsumo() != null ? dto.getDataConsumo() : LocalDateTime.now());

        return toRespostaDto(consumoInsumoInterface.save(consumo));
    }

    private void validaPermissaoEdicao(EUsuario usuario, EConsumoInsumo consumo) {
        if (usuario.getPerfil() == EnPerfilUsuario.ADMINISTRADOR || usuario.getPerfil() == EnPerfilUsuario.GERENTE) {
            return;
        }
        if (consumo.getRegistradoPorEmail() != null
                && consumo.getRegistradoPorEmail().trim().equalsIgnoreCase(usuario.getEmail().trim())) {
            return;
        }
        throw new IllegalArgumentException("Você só pode editar lançamentos que você mesmo criou.");
    }

    private void validaDataNaoFutura(LocalDateTime dataConsumo) {
        if (dataConsumo != null && dataConsumo.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data de consumo não pode ser no futuro.");
        }
    }

    @Transactional
    public List<ConsumoInsumoRespostaDto> listarPorSetor(Long setorId, LocalDate dataInicio, LocalDate dataFim) {
        List<EConsumoInsumo> lista;
        if (dataInicio != null && dataFim != null) {
            lista = consumoInsumoInterface.findBySetor_IdAndStatusAndDataConsumoBetweenOrderByDataConsumoDesc(
                    setorId, EnStatus.A, dataInicio.atStartOfDay(), dataFim.atTime(23, 59, 59));
        } else {
            lista = consumoInsumoInterface.findBySetor_IdAndStatusOrderByDataConsumoDesc(setorId, EnStatus.A);
        }
        return lista.stream().map(this::toRespostaDto).collect(Collectors.toList());
    }

    @Transactional
    public List<ConsumoInsumoResumoItemDto> resumoPorSetorEPeriodo(Long setorId, LocalDate dataInicio, LocalDate dataFim) {
        List<IConsumoInsumo.ResumoInsumoProjection> agregados = consumoInsumoInterface.resumoPorSetorEPeriodo(
                setorId, EnStatus.A, dataInicio.atStartOfDay(), dataFim.atTime(23, 59, 59));

        Map<Long, EInsumo> insumosPorId = insumoInterface
                .findAllById(agregados.stream().map(IConsumoInsumo.ResumoInsumoProjection::getInsumoId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EInsumo::getId, i -> i, (a, b) -> a, HashMap::new));

        return agregados.stream().map(a -> {
            ConsumoInsumoResumoItemDto item = new ConsumoInsumoResumoItemDto();
            item.setInsumoId(a.getInsumoId());
            item.setQuantidadeTotal(a.getTotal());
            EInsumo insumo = insumosPorId.get(a.getInsumoId());
            if (insumo != null) {
                item.setInsumoNome(insumo.getNome());
                if (insumo.getUnidadeMedidaPrimaria() != null) {
                    item.setUnidadeSigla(insumo.getUnidadeMedidaPrimaria().getUnidade());
                }
            }
            return item;
        }).collect(Collectors.toList());
    }

    // ── Helpers de conversão e rateio ───────────────────────────────────

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

    /**
     * Converte a quantidade informada para a unidade primária (a mesma em que saldoAtual é controlado).
     * Se a unidade de registro for a secundária, divide pelo fator de conversão
     * (ex: 40 KG / 40 (kg por saca) = 1 SACA).
     */
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

    /** Soma os animais de todas as alocações (lote_setor) ativas do setor, ignorando lotes inativados. */
    private int contarAnimaisDoSetor(Long setorId) {
        List<ELoteSetor> alocacoes = loteSetorInterface.findBySetor_Id(setorId);
        return alocacoes.stream()
                .filter(ls -> ls.getLote() != null && ls.getLote().getStatus() == EnStatus.A)
                .mapToInt(ls -> ls.getAnimais().size())
                .sum();
    }

    private ConsumoInsumoRespostaDto toRespostaDto(EConsumoInsumo consumo) {
        ConsumoInsumoRespostaDto dto = new ConsumoInsumoRespostaDto();
        dto.setId(consumo.getId());

        dto.setInsumoId(consumo.getInsumo().getId());
        dto.setInsumoNome(consumo.getInsumo().getNome());

        dto.setSetorId(consumo.getSetor().getId());
        dto.setSetorNome(consumo.getSetor().getNome());

        dto.setQuantidadeRegistrada(consumo.getQuantidadeRegistrada());
        dto.setUnidadeRegistroSigla(consumo.getUnidadeRegistro().getUnidade());

        dto.setQuantidadeBaixaUnidadePrimaria(consumo.getQuantidadeBaixaUnidadePrimaria());
        if (consumo.getInsumo().getUnidadeMedidaPrimaria() != null) {
            dto.setUnidadeMedidaPrimariaSigla(consumo.getInsumo().getUnidadeMedidaPrimaria().getUnidade());
        }

        dto.setTotalAnimaisSetor(consumo.getTotalAnimaisSetor());
        dto.setConsumoPorAnimal(consumo.getConsumoPorAnimal());
        dto.setSaldoAtualAposConsumo(consumo.getInsumo().getSaldoAtual());

        dto.setDataConsumo(consumo.getDataConsumo());
        dto.setRegistradoPorEmail(consumo.getRegistradoPorEmail());

        if (consumo.getRegistradoPorEmail() != null) {
            usuarioInterface.findByEmailAndStatus(consumo.getRegistradoPorEmail(), EnStatus.A)
                    .ifPresent(u -> dto.setRegistradoPorNome(u.getNome()));
        }

        return dto;
    }
}
