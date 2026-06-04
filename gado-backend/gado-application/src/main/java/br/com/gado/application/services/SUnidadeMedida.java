package br.com.gado.application.services;

import br.com.gado.application.dto.UnidadeMedidaDTO;
import br.com.gado.domain.entities.EUnidadeMedida;
import br.com.gado.domain.enums.EnStatus;
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
        EUnidadeMedida novaUnidadeMedida = modelMapper.map(unidadeMedidaDto, EUnidadeMedida.class);

        try {
            EUnidadeMedida unidadeMedidaSalva = this.unidadeMedidaInterface.save(novaUnidadeMedida);
            return modelMapper.map(unidadeMedidaSalva, UnidadeMedidaDTO.class);
        } catch (Exception e) {
            log.error("Erro ao criar nova unidade de medida: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UnidadeMedidaDTO bucarUnidadeMedidaPorId(Long unidadeMedidaId) {
        EUnidadeMedida existingEntity = this.unidadeMedidaInterface
                .findById(unidadeMedidaId)
                .orElseThrow(EntityNotFoundException::new);
        return modelMapper.map(existingEntity, UnidadeMedidaDTO.class);
    }

    public UnidadeMedidaDTO atualizarUnidadeMedida(Long unidadeMedidaId, UnidadeMedidaDTO unidadeMedidaDto) {
        EUnidadeMedida existingEntity = this.unidadeMedidaInterface
                .findById(unidadeMedidaId)
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        this.modelMapper.map(unidadeMedidaDto, existingEntity);

        try {
            EUnidadeMedida unidadeMedidaAtualizada = this.unidadeMedidaInterface.save(existingEntity);
            return modelMapper.map(unidadeMedidaAtualizada, UnidadeMedidaDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar unidade de medida: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String excluirUnidadeMedida(Long unidadeMedidaId) {
        EUnidadeMedida existingEntity = this.unidadeMedidaInterface
                .findById(unidadeMedidaId)
                .orElseThrow(EntityNotFoundException::new);

        existingEntity.setStatus(EnStatus.I);

        try {
            this.unidadeMedidaInterface.save(existingEntity);
            return "unidade de medida excluída com sucesso";
        } catch (Exception e) {
            log.error("Erro ao excluir unidade de medida: {}", e.getMessage(), e);
            return "erro ao excluir unidade de medida";
        }
    }
}
