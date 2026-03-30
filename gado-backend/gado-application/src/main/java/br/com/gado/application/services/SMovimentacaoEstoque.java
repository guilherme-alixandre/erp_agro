package br.com.gado.application.services;

import br.com.gado.domain.entities.EMovimentacaoEstoque;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.application.dto.MovimentacaoEstoqueDTO;
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

        EMovimentacaoEstoque novaMovimentacao = new EMovimentacaoEstoque();

        novaMovimentacao.setEnTipoMovimentacaoEstoque(novaMovimentacaoEstoque.getEnTipoMovimentacaoEstoque());
        novaMovimentacao.setQuantidade(novaMovimentacaoEstoque.getQuantidade());
        novaMovimentacao.setValorUnitario(novaMovimentacaoEstoque.getValorUnitario());
        novaMovimentacao.setDataMovimentacao(novaMovimentacaoEstoque.getDataMovimentacao());
        novaMovimentacao.setLoteId(novaMovimentacaoEstoque.getLoteId());
        novaMovimentacao.setParceiroId(novaMovimentacaoEstoque.getParceiroId());
        novaMovimentacao.setId(novaMovimentacaoEstoque.getId());
        novaMovimentacao.setAnimalId(novaMovimentacaoEstoque.getAnimalId());

        try{
            EMovimentacaoEstoque movimentacaoEstoque = this.movimentacaoEstoqueInterface.save(novaMovimentacao);
            return modelMapper.map(movimentacaoEstoque, MovimentacaoEstoqueDTO.class);
        } catch (Exception e) {
            log.error("Erro ao criar movimentacao estoque: {}", e.getMessage(), e);
            throw e;
        }
    }

    public MovimentacaoEstoqueDTO buscarMovimentacaoEstoquePorId(MovimentacaoEstoqueDTO movimentacaoEstoque) {
        EMovimentacaoEstoque existingEntitye = this.movimentacaoEstoqueInterface
                .findById(movimentacaoEstoque.getId())
                .orElseThrow(EntityNotFoundException::new);
        return modelMapper.map(existingEntitye, MovimentacaoEstoqueDTO.class);
    }

    public MovimentacaoEstoqueDTO atualizarMovimentacaoEstoquePorId(MovimentacaoEstoqueDTO movimentacaoEstoqueParaAtualizar) {
        EMovimentacaoEstoque existingEntitye = this.movimentacaoEstoqueInterface
                .findById(movimentacaoEstoqueParaAtualizar.getId())
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);

        this.modelMapper.map(movimentacaoEstoqueParaAtualizar, existingEntitye);

        try {
            EMovimentacaoEstoque movimentacaoEstoqueAtualizada = this.movimentacaoEstoqueInterface.save(existingEntitye);
            return modelMapper.map(movimentacaoEstoqueAtualizada, MovimentacaoEstoqueDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar movimentacao estoque: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean excluirMovimentacaoEstoquePorId(Long movimentacaoEstoqueId) {
        EMovimentacaoEstoque existingEntitye = this.movimentacaoEstoqueInterface
                .findById(movimentacaoEstoqueId)
                .orElseThrow(EntityNotFoundException::new);

        existingEntitye.setStatus(EnStatus.I);

        try{
            this.movimentacaoEstoqueInterface.save(existingEntitye);
            return true;
        } catch (Exception e){
            log.error("Erro ao excluir movimentação de estoque: {}", e.getMessage(), e);
        }

        return false;
    }
}
