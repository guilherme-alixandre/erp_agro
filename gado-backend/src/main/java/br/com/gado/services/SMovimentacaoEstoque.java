package br.com.gado.services;

import br.com.gado.dto.MovimentacaoEstoqueDTO;
import br.com.gado.dto.movimentacaoEstoqueDto.MovimentacaoEstoqueRespostaDto;
import br.com.gado.entities.EMovimentacaoEstoque;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IMovimentacaoEstoque;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SMovimentacaoEstoque {
    private final IMovimentacaoEstoque movimentacaoEstoqueInterface;
    private static final Logger log = LoggerFactory.getLogger(SMovimentacaoEstoque.class);
    private final ModelMapper modelMapper;

    public SMovimentacaoEstoque(IMovimentacaoEstoque movimentacaoEstoqueInterface, ModelMapper modelMapper) {
        this.movimentacaoEstoqueInterface = movimentacaoEstoqueInterface;
        this.modelMapper = modelMapper;
    }

    /** Histórico de movimentações (hoje: só entradas, registradas por SInsumo.aplicarEntradaEstoque). */
    public List<MovimentacaoEstoqueRespostaDto> listarTodas(Long insumoId) {
        List<EMovimentacaoEstoque> movimentacoes = insumoId != null
                ? movimentacaoEstoqueInterface.findByInsumoId_IdOrderByDataMovimentacaoDesc(insumoId)
                : movimentacaoEstoqueInterface.findAllByOrderByDataMovimentacaoDesc();

        return movimentacoes.stream().map(this::toRespostaDto).collect(Collectors.toList());
    }

    private MovimentacaoEstoqueRespostaDto toRespostaDto(EMovimentacaoEstoque movimentacao) {
        MovimentacaoEstoqueRespostaDto dto = new MovimentacaoEstoqueRespostaDto();
        dto.setId(movimentacao.getId());
        dto.setTipo(movimentacao.getEnTipoMovimentacaoEstoque());
        dto.setQuantidade(movimentacao.getQuantidade());
        dto.setValorUnitario(movimentacao.getValorUnitario());
        dto.setDataMovimentacao(movimentacao.getDataMovimentacao());

        if (movimentacao.getInsumoId() != null) {
            dto.setInsumoId(movimentacao.getInsumoId().getId());
            dto.setInsumoNome(movimentacao.getInsumoId().getNome());
            if (movimentacao.getInsumoId().getUnidadeMedidaPrimaria() != null) {
                dto.setUnidadeMedidaSigla(movimentacao.getInsumoId().getUnidadeMedidaPrimaria().getUnidade());
            }
        }
        if (movimentacao.getParceiroId() != null) {
            dto.setParceiroId(movimentacao.getParceiroId().getId());
            dto.setParceiroNome(movimentacao.getParceiroId().getNome());
        }
        if (movimentacao.getSetorId() != null) {
            dto.setSetorId(movimentacao.getSetorId().getId());
            dto.setSetorNome(movimentacao.getSetorId().getNome());
        }
        if (movimentacao.getAnimalId() != null) {
            dto.setAnimalId(movimentacao.getAnimalId().getId());
            dto.setAnimalCodigoBrinco(movimentacao.getAnimalId().getCodigoBrinco());
        }

        return dto;
    }

    public MovimentacaoEstoqueDTO criarMovimentacaoEstoque(MovimentacaoEstoqueDTO novaMovimentacaoEstoque) {
        EMovimentacaoEstoque novaMovimentacao = modelMapper.map(novaMovimentacaoEstoque, EMovimentacaoEstoque.class);

        try {
            EMovimentacaoEstoque movimentacaoEstoque = this.movimentacaoEstoqueInterface.save(novaMovimentacao);
            return modelMapper.map(movimentacaoEstoque, MovimentacaoEstoqueDTO.class);
        } catch (Exception e) {
            log.error("Erro ao criar movimentacao estoque: {}", e.getMessage(), e);
            throw e;
        }
    }

    public MovimentacaoEstoqueDTO buscarMovimentacaoEstoquePorId(Long movimentacaoEstoqueId) {
        EMovimentacaoEstoque existingEntity = this.movimentacaoEstoqueInterface
                .findById(movimentacaoEstoqueId)
                .orElseThrow(EntityNotFoundException::new);
        return modelMapper.map(existingEntity, MovimentacaoEstoqueDTO.class);
    }

    public MovimentacaoEstoqueDTO atualizarMovimentacaoEstoquePorId(Long movimentacaoEstoqueId, MovimentacaoEstoqueDTO movimentacaoEstoqueParaAtualizar) {
        EMovimentacaoEstoque existingEntity = this.movimentacaoEstoqueInterface
                .findById(movimentacaoEstoqueId)
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        this.modelMapper.map(movimentacaoEstoqueParaAtualizar, existingEntity);

        try {
            EMovimentacaoEstoque movimentacaoEstoqueAtualizada = this.movimentacaoEstoqueInterface.save(existingEntity);
            return modelMapper.map(movimentacaoEstoqueAtualizada, MovimentacaoEstoqueDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar movimentacao estoque: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String excluirMovimentacaoEstoquePorId(Long movimentacaoEstoqueId) {
        EMovimentacaoEstoque existingEntity = this.movimentacaoEstoqueInterface
                .findById(movimentacaoEstoqueId)
                .orElseThrow(EntityNotFoundException::new);

        existingEntity.setStatus(EnStatus.I);

        try {
            this.movimentacaoEstoqueInterface.save(existingEntity);
            return "movimentação de estoque excluída com sucesso";
        } catch (Exception e) {
            log.error("Erro ao excluir movimentação de estoque: {}", e.getMessage(), e);
            return "erro ao excluir movimentação de estoque";
        }
    }
}
