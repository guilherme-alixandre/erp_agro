package br.com.gado.application.services;

import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.EParceiro;
import br.com.gado.domain.entities.ETransacao;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.dto.TrasacaoDTO;
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
                .findByCPF_CNPJ(trasacaoDto.getParceiro().getCPF_CNPJ())
                .orElseThrow(EntityNotFoundException::new);

        ELote existingLote = this.loteInterface
                .findByLoteId(trasacaoDto.getLote().getId())
                .orElseThrow(EntityNotFoundException::new);

        ETransacao novaTransacao = new ETransacao();

        novaTransacao.setLote(existingLote);
        novaTransacao.setParceiro(existingParceiro);

        novaTransacao.setData(trasacaoDto.getData());
        novaTransacao.setValor(trasacaoDto.getValor());

        try {
            this.trasacaoInterface.save(novaTransacao);
            return modelMapper.map(novaTransacao, TrasacaoDTO.class);
        } catch (Exception e) {
            log.error("Erro ao criar nova transação Id: {}", e.getMessage(), e);
            throw e;
        }
    }

    public TrasacaoDTO buscarTrasacaoPorId(TrasacaoDTO trasacaoDto) {

        ETransacao existingEntitye = this.trasacaoInterface
                .findByTrasacaoId(trasacaoDto.getId())
                .orElseThrow(EntityNotFoundException::new);

        return modelMapper.map(existingEntitye, TrasacaoDTO.class);
    }

    public TrasacaoDTO atualizarTrasacaoPorId(TrasacaoDTO trasacaoDto) {

        ETransacao existingEntitye = this.trasacaoInterface
                .findByTrasacaoId(trasacaoDto.getId())
                .orElseThrow(EntityNotFoundException::new);

        EParceiro existingParceiro = this.parceiroInterface
                .findByCPF_CNPJ(trasacaoDto.getParceiro().getCPF_CNPJ())
                .orElseThrow(EntityNotFoundException::new);

        ELote existingLote = this.loteInterface
                .findByLoteId(trasacaoDto.getLote().getId())
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);

        this.modelMapper.map(trasacaoDto, existingEntitye);

        try {
            this.trasacaoInterface.save(existingEntitye);
            return modelMapper.map(existingEntitye, TrasacaoDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar trasacao Id: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean excluirTrasacaoPorId(Long trasacaoId) {
        ETransacao existingEntitye = this.trasacaoInterface
                .findByTrasacaoId(trasacaoId)
                .orElseThrow(EntityNotFoundException::new);

        existingEntitye.setStatus(EnStatus.I);

        try {
            this.trasacaoInterface.save(existingEntitye);
            return true;
        } catch (Exception e) {
            log.error("Erro ao excluir trasacao Id: {}", e.getMessage(), e);
            return false;
        }
    }
}
