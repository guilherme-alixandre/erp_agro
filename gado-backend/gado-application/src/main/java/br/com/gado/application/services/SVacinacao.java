package br.com.gado.application.services;

import br.com.gado.domain.entities.EVacinacao;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.application.dto.VacinacaoDTO;
import br.com.gado.infrastructure.persistence.repositories.IInsumo;
import br.com.gado.infrastructure.persistence.repositories.ILote;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import br.com.gado.infrastructure.persistence.repositories.IVacinacao;
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

        EVacinacao vacinacao = new EVacinacao();

        vacinacao.setDataOcorrencia(novaVacinacao.getDataOcorrencia());
        vacinacao.setUsuarioRelacionado(novaVacinacao.getUsuarioRelacionado());
        vacinacao.setInsumoRelacionado(novaVacinacao.getInsumoRelacionado());
        vacinacao.setInsumoRelacionado(novaVacinacao.getInsumoRelacionado());

        try {
            EVacinacao vaciacaoSalva = modelMapper.map(novaVacinacao, EVacinacao.class);
            return modelMapper.map(vaciacaoSalva, VacinacaoDTO.class);
        } catch (Exception e) {
            log.error("Erro ao criar registro de vacinação: {}", e.getMessage(), e);
            throw e;
        }
    }

    public VacinacaoDTO buscarVacinacaoPorId(VacinacaoDTO vacinacao) {
        EVacinacao existingEntitye = this.vacinacaoInterface
                .findById(vacinacao.getId())
                .orElseThrow(EntityNotFoundException::new);
        return modelMapper.map(existingEntitye, VacinacaoDTO.class);
    }

    public VacinacaoDTO atualizarVacinacaoPorId(VacinacaoDTO vacinacaoParaAtualizar) {
        EVacinacao existingEntitye = this.vacinacaoInterface
                .findById(vacinacaoParaAtualizar.getId())
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);

        this.modelMapper.map(vacinacaoParaAtualizar, existingEntitye);

        EVacinacao vacinacaoAtualizada = modelMapper.map(existingEntitye, EVacinacao.class);

        try{
            EVacinacao vacinacaoSalva = this.vacinacaoInterface.save(vacinacaoAtualizada);
            return modelMapper.map(vacinacaoSalva, VacinacaoDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar vacinação: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean excluirVacinacaoPorId(Long vacinacaoId) {
        EVacinacao existingEntitye = this.vacinacaoInterface
                .findById(vacinacaoId)
                .orElseThrow(EntityNotFoundException::new);

        existingEntitye.setStatus(EnStatus.I);

        try{
            this.vacinacaoInterface.save(existingEntitye);
            return true;
        } catch (Exception e){
            log.error("Erro ao excluir vacinação: {}", e.getMessage(), e);
        }

        return false;

    }
}
