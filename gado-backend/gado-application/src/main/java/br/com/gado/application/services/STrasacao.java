package br.com.gado.application.services;

import br.com.gado.application.dto.TrasacaoDTO;
import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.EParceiro;
import br.com.gado.domain.entities.ETransacao;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.ILote;
import br.com.gado.infrastructure.persistence.repositories.IParceiro;
import br.com.gado.infrastructure.persistence.repositories.ITrasacao;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class STrasacao {
    private final ITrasacao trasacaoInterface;
    private final IParceiro parceiroInterface;
    private final ILote loteInterface;
    private final ModelMapper modelMapper;
    private static final Logger log = LoggerFactory.getLogger(STrasacao.class);

    public STrasacao(ITrasacao trasacaoInterface, IParceiro parceiroInterface, ILote loteInterface, ModelMapper modelMapper) {
        this.trasacaoInterface = trasacaoInterface;
        this.parceiroInterface = parceiroInterface;
        this.loteInterface = loteInterface;
        this.modelMapper = modelMapper;
    }

    public TrasacaoDTO criarTrasacao(TrasacaoDTO trasacaoDto) {
        EParceiro existingParceiro = this.parceiroInterface
                .findByCpfCnpj(trasacaoDto.getParceiro().getCpfCnpj())
                .orElseThrow(EntityNotFoundException::new);

        ELote existingLote = this.loteInterface
                .findById(trasacaoDto.getLote().getId())
                .orElseThrow(EntityNotFoundException::new);

        ETransacao novaTransacao = modelMapper.map(trasacaoDto, ETransacao.class);
        novaTransacao.setLote(existingLote);
        novaTransacao.setParceiro(existingParceiro);

        try {
            ETransacao transacaoSalva = this.trasacaoInterface.save(novaTransacao);
            return modelMapper.map(transacaoSalva, TrasacaoDTO.class);
        } catch (Exception e) {
            log.error("Erro ao criar nova transaÃ§Ã£o Id: {}", e.getMessage(), e);
            throw e;
        }
    }

    public TrasacaoDTO buscarTrasacaoPorId(Long trasacaoId) {
        ETransacao existingEntity = this.trasacaoInterface
                .findById(trasacaoId)
                .orElseThrow(EntityNotFoundException::new);

        return modelMapper.map(existingEntity, TrasacaoDTO.class);
    }

    public TrasacaoDTO atualizarTrasacaoPorId(Long trasacaoId, TrasacaoDTO trasacaoDto) {
        ETransacao existingEntity = this.trasacaoInterface
                .findById(trasacaoId)
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        this.modelMapper.map(trasacaoDto, existingEntity);

        try {
            ETransacao transacaoAtualizada = this.trasacaoInterface.save(existingEntity);
            return modelMapper.map(transacaoAtualizada, TrasacaoDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar trasacao Id: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String excluirTrasacaoPorId(Long trasacaoId) {
        ETransacao existingEntity = this.trasacaoInterface
                .findById(trasacaoId)
                .orElseThrow(EntityNotFoundException::new);

        existingEntity.setStatus(EnStatus.I);

        try {
            this.trasacaoInterface.save(existingEntity);
            return "transaÃ§Ã£o excluÃ­da com sucesso";
        } catch (Exception e) {
            log.error("Erro ao excluir trasacao Id: {}", e.getMessage(), e);
            return "erro ao excluir transaÃ§Ã£o";
        }
    }
}
