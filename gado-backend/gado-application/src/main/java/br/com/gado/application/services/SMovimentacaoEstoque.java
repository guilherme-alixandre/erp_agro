package br.com.gado.application.services;

import br.com.gado.application.dto.MovimentacaoEstoqueDTO;
import br.com.gado.domain.entities.EMovimentacaoEstoque;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IMovimentacaoEstoque;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SMovimentacaoEstoque {
    private final IMovimentacaoEstoque movimentacaoEstoqueInterface;
    private static final Logger log = LoggerFactory.getLogger(SMovimentacaoEstoque.class);
    private final ModelMapper modelMapper;

    public SMovimentacaoEstoque(IMovimentacaoEstoque movimentacaoEstoqueInterface, ModelMapper modelMapper) {
        this.movimentacaoEstoqueInterface = movimentacaoEstoqueInterface;
        this.modelMapper = modelMapper;
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
