package br.com.gado.services;

import br.com.gado.dto.vacinacaoAnimalDto.AnimalResumoItemDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalCadastroDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalCancelamentoDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalEdicaoDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalResumoItemDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalRespostaDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.ELote;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.entities.EVacinacaoAnimal;
import br.com.gado.entities.EVacinacaoAnimalItem;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import br.com.gado.repositories.IVacinacaoAnimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Serviço da funcionalidade "Vacinar Animais": aplicação de um insumo em um
 * ou mais animais (individuais ou de um lote inteiro), com baixa automática
 * de estoque (dose por animal x nº de animais). Não gera lançamento
 * financeiro (mesmo comportamento de "Alimentar Setores").
 */
@Service
public class SVacinacaoAnimal {

    @Autowired
    private IVacinacaoAnimal vacinacaoAnimalInterface;

    @Autowired
    private IInsumo insumoInterface;

    @Autowired
    private IAnimal animalInterface;

    @Autowired
    private ILote loteInterface;

    @Autowired
    private IUnidadeMedida unidadeMedidaInterface;

    @Autowired
    private IUsuario usuarioInterface;

    // ── Permissões ───────────────────────────────────────────────────────

    /** Qualquer usuário ativo pode registrar aplicação de vacina. */
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

    /** Apenas quem criou a aplicação ou um Administrador podem cancelá-la. */
    private void validaPermissaoCancelamento(EUsuario usuario, EVacinacaoAnimal vacinacao) {
        if (usuario.getPerfil() == EnPerfilUsuario.ADMINISTRADOR) {
            return;
        }
        if (vacinacao.getCriadoPorEmail() != null
                && vacinacao.getCriadoPorEmail().trim().equalsIgnoreCase(usuario.getEmail().trim())) {
            return;
        }
        throw new IllegalArgumentException("Você só pode cancelar aplicações que você mesmo criou.");
    }

    /** Autor da aplicação, Gerente ou Administrador podem editar. */
    private void validaPermissaoEdicao(EUsuario usuario, EVacinacaoAnimal vacinacao) {
        if (usuario.getPerfil() == EnPerfilUsuario.ADMINISTRADOR || usuario.getPerfil() == EnPerfilUsuario.GERENTE) {
            return;
        }
        if (vacinacao.getCriadoPorEmail() != null
                && vacinacao.getCriadoPorEmail().trim().equalsIgnoreCase(usuario.getEmail().trim())) {
            return;
        }
        throw new IllegalArgumentException("Você só pode editar aplicações que você mesmo criou.");
    }

    private void validaDataNaoFutura(LocalDateTime dataAplicacao) {
        if (dataAplicacao != null && dataAplicacao.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data de aplicação não pode ser no futuro.");
        }
    }

    // ── Vacinar Animais ──────────────────────────────────────────────────

    @Transactional
    public VacinacaoAnimalRespostaDto registrarAplicacao(VacinacaoAnimalCadastroDto dto, String emailUsuario) {
        resolveUsuarioObrigatorio(emailUsuario);
        validaDataNaoFutura(dto.getDataAplicacao());

        EInsumo insumo = insumoInterface.findByIdAndStatus(dto.getInsumoId(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado ou inativo."));

        if (insumo.getUnidadeMedidaPrimaria() == null) {
            throw new IllegalArgumentException(
                    "Este produto não possui unidade de medida cadastrada e não pode ter aplicação registrada.");
        }

        List<EAnimal> animais = resolveAnimais(dto.getAnimalIds());

        EUnidadeMedida unidadeRegistro = resolveUnidadeRegistro(insumo, dto.getUnidadeMedidaId());
        double quantidadeBaixaPorAnimal = converterParaUnidadePrimaria(insumo, unidadeRegistro, dto.getQuantidadePorAnimal());
        double quantidadeTotalBaixa = quantidadeBaixaPorAnimal * animais.size();

        double saldoAtual = insumo.getSaldoAtual() != null ? insumo.getSaldoAtual() : 0.0;
        if (saldoAtual < quantidadeTotalBaixa) {
            throw new IllegalArgumentException(String.format(
                    "Estoque insuficiente para esta aplicação. Saldo atual: %.2f %s.",
                    saldoAtual, insumo.getUnidadeMedidaPrimaria().getUnidade()));
        }

        insumo.setSaldoAtual(saldoAtual - quantidadeTotalBaixa);
        insumoInterface.save(insumo);

        EVacinacaoAnimal vacinacao = new EVacinacaoAnimal();
        vacinacao.setInsumo(insumo);
        vacinacao.setQuantidadePorAnimal(dto.getQuantidadePorAnimal());
        vacinacao.setUnidadeRegistro(unidadeRegistro);
        vacinacao.setQuantidadeBaixaPorAnimalUnidadePrimaria(quantidadeBaixaPorAnimal);
        vacinacao.setQuantidadeTotalBaixaUnidadePrimaria(quantidadeTotalBaixa);
        vacinacao.setDataAplicacao(dto.getDataAplicacao() != null ? dto.getDataAplicacao() : LocalDateTime.now());
        vacinacao.setCriadoPorEmail(emailUsuario.trim());
        vacinacao.setCancelado(false);

        if (dto.getLoteId() != null) {
            ELote lote = loteInterface.findById(dto.getLoteId())
                    .orElseThrow(() -> new IllegalArgumentException("Lote não encontrado."));
            vacinacao.setLote(lote);
        }

        List<EVacinacaoAnimalItem> itens = new ArrayList<>();
        for (EAnimal animal : animais) {
            EVacinacaoAnimalItem item = new EVacinacaoAnimalItem();
            item.setVacinacao(vacinacao);
            item.setAnimal(animal);
            itens.add(item);
        }
        vacinacao.setItens(itens);

        EVacinacaoAnimal salvo = vacinacaoAnimalInterface.save(vacinacao);
        return toRespostaDto(salvo);
    }

    @Transactional
    public VacinacaoAnimalRespostaDto cancelarAplicacao(Long id, VacinacaoAnimalCancelamentoDto dto, String emailUsuario) {
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);

        EVacinacaoAnimal vacinacao = vacinacaoAnimalInterface.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aplicação não encontrada."));

        if (Boolean.TRUE.equals(vacinacao.getCancelado())) {
            throw new IllegalArgumentException("Esta aplicação já foi cancelada.");
        }

        validaPermissaoCancelamento(usuario, vacinacao);

        EInsumo insumo = vacinacao.getInsumo();
        double saldoAtual = insumo.getSaldoAtual() != null ? insumo.getSaldoAtual() : 0.0;
        insumo.setSaldoAtual(saldoAtual + vacinacao.getQuantidadeTotalBaixaUnidadePrimaria());
        insumoInterface.save(insumo);

        vacinacao.setCancelado(true);
        vacinacao.setMotivoCancelamento(dto.getMotivoCancelamento().trim());
        vacinacao.setCanceladoPorEmail(emailUsuario.trim());
        vacinacao.setCanceladoEm(LocalDateTime.now());

        return toRespostaDto(vacinacaoAnimalInterface.save(vacinacao));
    }

    /** Autor, Gerente ou Administrador podem editar; reverte a baixa antiga e reaplica a nova. Não altera os animais cobertos. */
    @Transactional
    public VacinacaoAnimalRespostaDto editarAplicacao(Long id, VacinacaoAnimalEdicaoDto dto, String emailUsuario) {
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);

        EVacinacaoAnimal vacinacao = vacinacaoAnimalInterface.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aplicação não encontrada."));

        if (Boolean.TRUE.equals(vacinacao.getCancelado())) {
            throw new IllegalArgumentException("Não é possível editar uma aplicação já cancelada.");
        }

        validaPermissaoEdicao(usuario, vacinacao);
        validaDataNaoFutura(dto.getDataAplicacao());

        EInsumo insumo = vacinacao.getInsumo();

        // Estorna a baixa anterior antes de recalcular com os novos valores.
        double saldoComEstorno = (insumo.getSaldoAtual() != null ? insumo.getSaldoAtual() : 0.0)
                + vacinacao.getQuantidadeTotalBaixaUnidadePrimaria();

        EUnidadeMedida unidadeRegistro = resolveUnidadeRegistro(insumo, dto.getUnidadeMedidaId());
        double novaQuantidadeBaixaPorAnimal = converterParaUnidadePrimaria(insumo, unidadeRegistro, dto.getQuantidadePorAnimal());
        int totalAnimais = vacinacao.getItens().size();
        double novaQuantidadeTotalBaixa = novaQuantidadeBaixaPorAnimal * totalAnimais;

        if (saldoComEstorno < novaQuantidadeTotalBaixa) {
            throw new IllegalArgumentException(String.format(
                    "Estoque insuficiente para esta edição. Saldo disponível: %.2f %s.",
                    saldoComEstorno, insumo.getUnidadeMedidaPrimaria().getUnidade()));
        }

        insumo.setSaldoAtual(saldoComEstorno - novaQuantidadeTotalBaixa);
        insumoInterface.save(insumo);

        vacinacao.setQuantidadePorAnimal(dto.getQuantidadePorAnimal());
        vacinacao.setUnidadeRegistro(unidadeRegistro);
        vacinacao.setQuantidadeBaixaPorAnimalUnidadePrimaria(novaQuantidadeBaixaPorAnimal);
        vacinacao.setQuantidadeTotalBaixaUnidadePrimaria(novaQuantidadeTotalBaixa);
        vacinacao.setDataAplicacao(dto.getDataAplicacao() != null ? dto.getDataAplicacao() : LocalDateTime.now());

        return toRespostaDto(vacinacaoAnimalInterface.save(vacinacao));
    }

    @Transactional
    public List<VacinacaoAnimalRespostaDto> listarTodos(LocalDate dataInicio, LocalDate dataFim) {
        List<EVacinacaoAnimal> lista = (dataInicio != null && dataFim != null)
                ? vacinacaoAnimalInterface.findByDataAplicacaoBetweenOrderByDataAplicacaoDesc(
                        dataInicio.atStartOfDay(), dataFim.atTime(23, 59, 59))
                : vacinacaoAnimalInterface.findAllByOrderByDataAplicacaoDesc();

        return lista.stream().map(this::toRespostaDto).collect(Collectors.toList());
    }

    @Transactional
    public List<VacinacaoAnimalRespostaDto> listarPorAnimal(Long animalId) {
        return vacinacaoAnimalInterface.findByAnimalId(animalId).stream()
                .map(this::toRespostaDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<VacinacaoAnimalResumoItemDto> resumoPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        List<EVacinacaoAnimal> lista = vacinacaoAnimalInterface.findByDataAplicacaoBetweenOrderByDataAplicacaoDesc(
                dataInicio.atStartOfDay(), dataFim.atTime(23, 59, 59));

        Map<Long, VacinacaoAnimalResumoItemDto> resumoPorInsumo = new LinkedHashMap<>();
        for (EVacinacaoAnimal vacinacao : lista) {
            if (Boolean.TRUE.equals(vacinacao.getCancelado())) continue;

            EInsumo insumo = vacinacao.getInsumo();
            VacinacaoAnimalResumoItemDto acumulado = resumoPorInsumo.computeIfAbsent(insumo.getId(), k -> {
                VacinacaoAnimalResumoItemDto novo = new VacinacaoAnimalResumoItemDto();
                novo.setInsumoId(insumo.getId());
                novo.setInsumoNome(insumo.getNome());
                novo.setQuantidadeTotal(0.0);
                if (insumo.getUnidadeMedidaPrimaria() != null) {
                    novo.setUnidadeSigla(insumo.getUnidadeMedidaPrimaria().getUnidade());
                }
                return novo;
            });
            acumulado.setQuantidadeTotal(acumulado.getQuantidadeTotal() + vacinacao.getQuantidadeTotalBaixaUnidadePrimaria());
        }
        return new ArrayList<>(resumoPorInsumo.values());
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private List<EAnimal> resolveAnimais(List<Long> animalIds) {
        List<EAnimal> animais = animalInterface.findAllById(animalIds).stream()
                .filter(a -> a.getStatus() == EnStatus.A)
                .collect(Collectors.toList());

        if (animais.size() != animalIds.size()) {
            throw new IllegalArgumentException("Um ou mais animais selecionados não foram encontrados ou estão inativos.");
        }
        return animais;
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

    private VacinacaoAnimalRespostaDto toRespostaDto(EVacinacaoAnimal vacinacao) {
        VacinacaoAnimalRespostaDto dto = new VacinacaoAnimalRespostaDto();
        dto.setId(vacinacao.getId());

        dto.setInsumoId(vacinacao.getInsumo().getId());
        dto.setInsumoNome(vacinacao.getInsumo().getNome());

        dto.setQuantidadePorAnimal(vacinacao.getQuantidadePorAnimal());
        dto.setUnidadeRegistroSigla(vacinacao.getUnidadeRegistro().getUnidade());

        dto.setQuantidadeBaixaPorAnimalUnidadePrimaria(vacinacao.getQuantidadeBaixaPorAnimalUnidadePrimaria());
        if (vacinacao.getInsumo().getUnidadeMedidaPrimaria() != null) {
            dto.setUnidadeMedidaPrimariaSigla(vacinacao.getInsumo().getUnidadeMedidaPrimaria().getUnidade());
        }
        dto.setQuantidadeTotalBaixaUnidadePrimaria(vacinacao.getQuantidadeTotalBaixaUnidadePrimaria());
        dto.setSaldoAtualAposAplicacao(vacinacao.getInsumo().getSaldoAtual());

        dto.setTotalAnimais(vacinacao.getItens().size());
        dto.setAnimais(vacinacao.getItens().stream().map(item -> {
            AnimalResumoItemDto animalDto = new AnimalResumoItemDto();
            animalDto.setId(item.getAnimal().getId());
            animalDto.setCodigoBrinco(item.getAnimal().getCodigoBrinco());
            animalDto.setRacaNome(item.getAnimal().getRaca() != null ? item.getAnimal().getRaca().getNome() : null);
            return animalDto;
        }).collect(Collectors.toList()));

        if (vacinacao.getLote() != null) {
            dto.setLoteId(vacinacao.getLote().getId());
            dto.setLoteCodigo(vacinacao.getLote().getCodigo());
        }

        dto.setDataAplicacao(vacinacao.getDataAplicacao());
        dto.setCriadoPorEmail(vacinacao.getCriadoPorEmail());
        dto.setCancelado(vacinacao.getCancelado());
        dto.setMotivoCancelamento(vacinacao.getMotivoCancelamento());
        dto.setCanceladoPorEmail(vacinacao.getCanceladoPorEmail());
        dto.setCanceladoEm(vacinacao.getCanceladoEm());

        if (vacinacao.getCriadoPorEmail() != null) {
            usuarioInterface.findByEmailAndStatus(vacinacao.getCriadoPorEmail(), EnStatus.A)
                    .ifPresent(u -> dto.setCriadoPorNome(u.getNome()));
        }
        if (vacinacao.getCanceladoPorEmail() != null) {
            usuarioInterface.findByEmailAndStatus(vacinacao.getCanceladoPorEmail(), EnStatus.A)
                    .ifPresent(u -> dto.setCanceladoPorNome(u.getNome()));
        }

        return dto;
    }
}
