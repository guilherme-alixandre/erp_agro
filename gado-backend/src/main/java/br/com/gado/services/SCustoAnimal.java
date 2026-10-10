package br.com.gado.services;

import br.com.gado.dto.custoAnimalDto.CustoVidaAnimalDto;
import br.com.gado.dto.custoAnimalDto.CustoVidaEventoDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EConsumoInsumo;
import br.com.gado.entities.ECustoAnimal;
import br.com.gado.entities.EVacinacaoAnimal;
import br.com.gado.entities.EVacinacaoAnimalItem;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoCustoAnimal;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.ICustoAnimal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ledger histórico de custo por animal (aquisição, alimentação e vacinação). Diferente do custo
 * de ração por lote (ver SLote.calcularCustoRacaoAcumulado), aqui o preço do insumo é fotografado
 * no momento do evento e o registro pertence ao animal — por isso o custo "viaja" com ele quando
 * troca de lote. Único ponto de escrita deste ledger (mesmo espírito de SLancamentoFinanceiro
 * para o DRE): os demais services chamam os métodos públicos daqui em vez de gravar em
 * ECustoAnimal diretamente.
 *
 * Limitação aceita: como os registros antigos de consumo/vacinação não guardam o preço pago na
 * época, o ledger só passa a existir a partir do momento em que esta feature foi implantada —
 * eventos passados não são reconstruídos retroativamente.
 */
@Service
public class SCustoAnimal {

    private static final String ORIGEM_AQUISICAO = "ANIMAL";
    private static final String ORIGEM_CONSUMO_INSUMO = "CONSUMO_INSUMO";
    private static final String ORIGEM_VACINACAO_ITEM = "VACINACAO_ITEM";

    @Autowired
    private ICustoAnimal custoAnimalInterface;

    @Autowired
    private IAnimal animalInterface;

    // ── Escrita do ledger ──────────────────────────────────────────────────

    /** Upsert idempotente: remove o lançamento de aquisição anterior (se houver) e recria. */
    @Transactional
    public void registrarOuAtualizarAquisicao(EAnimal animal) {
        estornarPorOrigem(ORIGEM_AQUISICAO, animal.getId());

        if (animal.getValorAquisicao() == null) {
            return;
        }

        ECustoAnimal custo = new ECustoAnimal();
        custo.setAnimal(animal);
        custo.setTipoCusto(EnTipoCustoAnimal.AQUISICAO);
        custo.setValor(animal.getValorAquisicao());
        custo.setDataEvento(animal.getDataAquisicao() != null ? animal.getDataAquisicao() : LocalDateTime.now());
        custo.setOrigemTipo(ORIGEM_AQUISICAO);
        custo.setOrigemId(animal.getId());
        custo.setDescricao("Aquisição do animal " + animal.getCodigoBrinco());
        custoAnimalInterface.save(custo);
    }

    /**
     * Cria um lançamento de custo de alimentação por animal, rateado a partir do consumo do
     * setor (EConsumoInsumo.consumoPorAnimal) com o preço do insumo fotografado no momento. Em
     * edição, chamar de novo: o lançamento anterior daquele consumo é removido antes de recriar.
     */
    @Transactional
    public void registrarCustoAlimentacao(EConsumoInsumo consumo, List<EAnimal> animaisDoSetor, double precoUnitario) {
        estornarPorOrigem(ORIGEM_CONSUMO_INSUMO, consumo.getId());

        if (consumo.getConsumoPorAnimal() == null) {
            return;
        }

        BigDecimal valorPorAnimal = BigDecimal.valueOf(consumo.getConsumoPorAnimal() * precoUnitario)
                .setScale(2, RoundingMode.HALF_UP);

        for (EAnimal animal : animaisDoSetor) {
            ECustoAnimal custo = new ECustoAnimal();
            custo.setAnimal(animal);
            custo.setTipoCusto(EnTipoCustoAnimal.ALIMENTACAO);
            custo.setValor(valorPorAnimal);
            custo.setQuantidade(consumo.getConsumoPorAnimal());
            custo.setDataEvento(consumo.getDataConsumo());
            custo.setOrigemTipo(ORIGEM_CONSUMO_INSUMO);
            custo.setOrigemId(consumo.getId());
            custo.setDescricao("Ração: " + consumo.getInsumo().getNome() + " (" + consumo.getSetor().getNome() + ")");
            custo.setRegistradoPorEmail(consumo.getRegistradoPorEmail());
            custoAnimalInterface.save(custo);
        }
    }

    /** Remove os lançamentos de alimentação de um consumo (edição/exclusão do lançamento de origem). */
    @Transactional
    public void estornarCustoAlimentacao(Long consumoInsumoId) {
        estornarPorOrigem(ORIGEM_CONSUMO_INSUMO, consumoInsumoId);
    }

    /** Cria o lançamento de custo de vacinação de um item (um animal) de uma aplicação. */
    @Transactional
    public void registrarCustoVacinacao(EVacinacaoAnimalItem item, double precoUnitario) {
        EVacinacaoAnimal vacinacao = item.getVacinacao();
        BigDecimal valor = BigDecimal.valueOf(vacinacao.getQuantidadeBaixaPorAnimalUnidadePrimaria() * precoUnitario)
                .setScale(2, RoundingMode.HALF_UP);

        ECustoAnimal custo = new ECustoAnimal();
        custo.setAnimal(item.getAnimal());
        custo.setTipoCusto(EnTipoCustoAnimal.VACINACAO);
        custo.setValor(valor);
        custo.setQuantidade(vacinacao.getQuantidadePorAnimal());
        custo.setDataEvento(vacinacao.getDataAplicacao());
        custo.setOrigemTipo(ORIGEM_VACINACAO_ITEM);
        custo.setOrigemId(item.getId());
        custo.setDescricao("Vacina: " + vacinacao.getInsumo().getNome());
        custo.setRegistradoPorEmail(vacinacao.getCriadoPorEmail());
        custoAnimalInterface.save(custo);
    }

    /** Remove o lançamento de vacinação de um item (edição/cancelamento da aplicação de origem). */
    @Transactional
    public void estornarCustoVacinacaoItem(Long itemId) {
        estornarPorOrigem(ORIGEM_VACINACAO_ITEM, itemId);
    }

    private void estornarPorOrigem(String origemTipo, Long origemId) {
        List<ECustoAnimal> existentes =
                custoAnimalInterface.findByOrigemTipoAndOrigemIdAndStatus(origemTipo, origemId, EnStatus.A);
        for (ECustoAnimal custo : existentes) {
            custo.setStatus(EnStatus.I);
        }
        custoAnimalInterface.saveAll(existentes);
    }

    // ── Leitura ──────────────────────────────────────────────────────────

    public CustoVidaAnimalDto calcularCustoVida(Long animalId) {
        animalInterface.findByIdAndStatus(animalId, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum animal ativo encontrado para o ID: " + animalId));

        List<ECustoAnimal> eventos =
                custoAnimalInterface.findByAnimal_IdAndStatusOrderByDataEventoDesc(animalId, EnStatus.A);
        return montarDto(animalId, eventos);
    }

    /** Usado por SLote.calcularCustoTotalAcumulado para somar o ledger dos animais atualmente no lote. */
    public Map<Long, CustoVidaAnimalDto> calcularCustoTotalAnimais(List<Long> animalIds) {
        if (animalIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<ECustoAnimal>> porAnimal = custoAnimalInterface
                .findByAnimal_IdInAndStatus(animalIds, EnStatus.A)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getAnimal().getId()));

        return animalIds.stream().distinct().collect(Collectors.toMap(
                id -> id,
                id -> montarDto(id, porAnimal.getOrDefault(id, List.of()))));
    }

    private CustoVidaAnimalDto montarDto(Long animalId, List<ECustoAnimal> eventos) {
        BigDecimal totalAquisicao = somaPorTipo(eventos, EnTipoCustoAnimal.AQUISICAO);
        BigDecimal totalAlimentacao = somaPorTipo(eventos, EnTipoCustoAnimal.ALIMENTACAO);
        BigDecimal totalVacinacao = somaPorTipo(eventos, EnTipoCustoAnimal.VACINACAO);
        BigDecimal totalGeral = totalAquisicao.add(totalAlimentacao).add(totalVacinacao);

        List<CustoVidaEventoDto> eventosDto = eventos.stream()
                .sorted(Comparator.comparing(ECustoAnimal::getDataEvento).reversed())
                .map(e -> new CustoVidaEventoDto(e.getTipoCusto(), e.getDataEvento(), e.getDescricao(), e.getValor()))
                .collect(Collectors.toList());

        return new CustoVidaAnimalDto(animalId, totalAquisicao, totalAlimentacao, totalVacinacao, totalGeral, eventosDto);
    }

    private BigDecimal somaPorTipo(List<ECustoAnimal> eventos, EnTipoCustoAnimal tipo) {
        return eventos.stream()
                .filter(e -> e.getTipoCusto() == tipo)
                .map(ECustoAnimal::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
