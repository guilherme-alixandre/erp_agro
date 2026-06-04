package br.com.gado.services;

import br.com.gado.dto.VacinacaoDTO;
import br.com.gado.entities.EVacinacao;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.IUsuario;
import br.com.gado.repositories.IVacinacao;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SVacinacao {

    private static final Logger log = LoggerFactory.getLogger(SVacinacao.class);

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private IVacinacao vacinacaoInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ILote loteInterface;

    @Autowired
    private IInsumo insumoInterface;

    @Transactional
    public VacinacaoDTO criarVacinacao(VacinacaoDTO novaVacinacao) {
        EVacinacao vacinacao = modelMapper.map(novaVacinacao, EVacinacao.class);

        try {
            EVacinacao vaciacaoSalva = this.vacinacaoInterface.save(vacinacao);
            return modelMapper.map(vaciacaoSalva, VacinacaoDTO.class);
        } catch (Exception e) {
            log.error("Erro ao criar registro de vacinação: {}", e.getMessage(), e);
            throw e;
        }
    }

    public VacinacaoDTO buscarVacinacaoPorId(Long vacinacaoId) {
        EVacinacao existingEntity = this.vacinacaoInterface
                .findById(vacinacaoId)
                .orElseThrow(EntityNotFoundException::new);
        return modelMapper.map(existingEntity, VacinacaoDTO.class);
    }

    public VacinacaoDTO atualizarVacinacaoPorId(Long vacinacaoId, VacinacaoDTO vacinacaoParaAtualizar) {
        EVacinacao existingEntity = this.vacinacaoInterface
                .findById(vacinacaoId)
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        this.modelMapper.map(vacinacaoParaAtualizar, existingEntity);

        try {
            EVacinacao vacinacaoSalva = this.vacinacaoInterface.save(existingEntity);
            return modelMapper.map(vacinacaoSalva, VacinacaoDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar vacinação: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String excluirVacinacaoPorId(Long vacinacaoId) {
        EVacinacao existingEntity = this.vacinacaoInterface
                .findById(vacinacaoId)
                .orElseThrow(EntityNotFoundException::new);

        existingEntity.setStatus(EnStatus.I);

        try {
            this.vacinacaoInterface.save(existingEntity);
            return "vacinação excluída com sucesso";
        } catch (Exception e) {
            log.error("Erro ao excluir vacinação: {}", e.getMessage(), e);
            return "erro ao excluir vacinação";
        }
    }
}
