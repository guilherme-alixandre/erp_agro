package br.com.gado.application.services;

import br.com.gado.domain.entities.EUnidadeMedida;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.application.dto.UnidadeMedidaDTO;
import br.com.gado.infrastructure.persistence.repositories.IUnidadeMedida;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SUnidadeMedida {

    private final IUnidadeMedida unidadeMedidaInterface;
    private static final Logger log = LoggerFactory.getLogger(SUnidadeMedida.class);
    private final ModelMapper modelMapper;

    public SUnidadeMedida(IUnidadeMedida unidadeMedidaInterface, ModelMapper modelMapper) {
        this.unidadeMedidaInterface = unidadeMedidaInterface;
        this.modelMapper = modelMapper;
    }

    public UnidadeMedidaDTO criarUnidadeMedida(UnidadeMedidaDTO unidadeMedidaDto) {
        EUnidadeMedida novaUnidadeMedida = new EUnidadeMedida();

        novaUnidadeMedida.setUnidade(unidadeMedidaDto.getUnidade());

        try {
            this.unidadeMedidaInterface.save(novaUnidadeMedida);
            return modelMapper.map(novaUnidadeMedida, UnidadeMedidaDTO.class);
        } catch (Exception e) {
            log.error("Erro ao criar nova unidade de medida: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UnidadeMedidaDTO bucarUnidadeMedidaPorId(UnidadeMedidaDTO unidadeMedidaDto) {
        EUnidadeMedida existingEntitye = this.unidadeMedidaInterface
                .findById(unidadeMedidaDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        return modelMapper.map(existingEntitye, UnidadeMedidaDTO.class);
    }

    public UnidadeMedidaDTO atualizarUnidadeMedida(UnidadeMedidaDTO unidadeMedidaDto) {
        EUnidadeMedida existingEntitye = this.unidadeMedidaInterface
                .findById(unidadeMedidaDto.getId())
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);

        this.modelMapper.map(unidadeMedidaDto, existingEntitye);

        try{
            this.unidadeMedidaInterface.save(existingEntitye);
            return modelMapper.map(existingEntitye, UnidadeMedidaDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar unidade de medida: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean excluirUnidadeMedida(Long unidadeMedidaId) {
        EUnidadeMedida existingEntitye = this.unidadeMedidaInterface
                .findById(unidadeMedidaId)
                .orElseThrow(EntityNotFoundException::new);

        existingEntitye.setStatus(EnStatus.I);

        try {
            this.unidadeMedidaInterface.save(existingEntitye);
            return true;
        } catch (Exception e) {
            log.error("Erro ao excluir unidade de medida: {}", e.getMessage(), e);
            return false;
        }
    }
}
