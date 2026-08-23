package br.com.gado.services;

import br.com.gado.dto.OcorrenciaAnimalDTO;
import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalCadastroDto;
import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalRespostaDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EOcorrenciaAnimal;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.IOcorrenciaAnimal;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SOcorrenciaAnimal {
    private static final Logger log = LoggerFactory.getLogger(SOcorrenciaAnimal.class);
    private final IOcorrenciaAnimal ocorrenciaAnimalInterface;
    private final IAnimal animalInterface;
    private final ModelMapper modelMapper;

    public SOcorrenciaAnimal(IOcorrenciaAnimal correnciaAnimalInterface, IAnimal animalInterface, ModelMapper modelMapper) {
        this.ocorrenciaAnimalInterface = correnciaAnimalInterface;
        this.animalInterface = animalInterface;
        this.modelMapper = modelMapper;
    }

    public OcorrenciaAnimalDTO criarOcorrenciaAnimal(OcorrenciaAnimalDTO ocorrenciaAnimalDto) {
        EAnimal existingAnimal = this.animalInterface
                .findByCodigoBrincoAndStatus(ocorrenciaAnimalDto.getIdAnimal().getCodigoBrinco(), ocorrenciaAnimalDto.getIdAnimal().getStatus())
                .orElseThrow(EntityNotFoundException::new);

        EOcorrenciaAnimal novaOcorrenciaAnimal = this.modelMapper.map(ocorrenciaAnimalDto, EOcorrenciaAnimal.class);
        novaOcorrenciaAnimal.setIdAnimal(existingAnimal);

        try {
            EOcorrenciaAnimal ocorrenciaAnimalSalva = this.ocorrenciaAnimalInterface.save(novaOcorrenciaAnimal);
            return this.modelMapper.map(ocorrenciaAnimalSalva, OcorrenciaAnimalDTO.class);
        } catch (Exception e) {
            log.error("Erro ao criar a ocorrência animal Id: {}", e.getMessage(), e);
            throw e;
        }
    }

    public OcorrenciaAnimalDTO encontrarOcorrenciaAnimalPorId(Long ocorrenciaAnimalId) {
        EOcorrenciaAnimal existingEntity = this.ocorrenciaAnimalInterface
                .findById(ocorrenciaAnimalId)
                .orElseThrow(EntityNotFoundException::new);
        return this.modelMapper.map(existingEntity, OcorrenciaAnimalDTO.class);
    }

    public OcorrenciaAnimalDTO atualizarOcorrenciaAnimal(Long ocorrenciaAnimalId, OcorrenciaAnimalDTO ocorrenciaAnimalDto) {
        EOcorrenciaAnimal existingEntity = this.ocorrenciaAnimalInterface
                .findById(ocorrenciaAnimalId)
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        this.modelMapper.map(ocorrenciaAnimalDto, existingEntity);

        try {
            EOcorrenciaAnimal ocorenciaAnimalAtualizada = this.ocorrenciaAnimalInterface.save(existingEntity);
            return modelMapper.map(ocorenciaAnimalAtualizada, OcorrenciaAnimalDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar a ocorrência animal Id: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<OcorrenciaAnimalRespostaDto> listarPorAnimal(Long animalId) {
        return ocorrenciaAnimalInterface
                .findByIdAnimal_IdAndStatusOrderByDataOcorrenciaDesc(animalId, EnStatus.A)
                .stream()
                .map(this::toRespostaDto)
                .collect(Collectors.toList());
    }

    public OcorrenciaAnimalRespostaDto criarOcorrenciaPorAnimalId(OcorrenciaAnimalCadastroDto dto) {
        EAnimal animal = animalInterface.findByIdAndStatus(dto.getAnimalId(), EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Animal não encontrado ou inativo."));

        EOcorrenciaAnimal ocorrencia = new EOcorrenciaAnimal();
        ocorrencia.setTipoOcorrencia(dto.getTipoOcorrencia());
        ocorrencia.setDataOcorrencia(dto.getDataOcorrencia());
        ocorrencia.setObservacao(dto.getObservacao());
        ocorrencia.setIdAnimal(animal);

        return toRespostaDto(ocorrenciaAnimalInterface.save(ocorrencia));
    }

    private OcorrenciaAnimalRespostaDto toRespostaDto(EOcorrenciaAnimal ocorrencia) {
        OcorrenciaAnimalRespostaDto dto = new OcorrenciaAnimalRespostaDto();
        dto.setId(ocorrencia.getId());
        dto.setTipoOcorrencia(ocorrencia.getTipoOcorrencia());
        dto.setDataOcorrencia(ocorrencia.getDataOcorrencia());
        dto.setObservacao(ocorrencia.getObservacao());
        return dto;
    }

    public String excluirOcorrenciaAnimal(Long ocorrenciaAnimalId) {
        EOcorrenciaAnimal existingEntity = this.ocorrenciaAnimalInterface
                .findById(ocorrenciaAnimalId)
                .orElseThrow(EntityNotFoundException::new);

        existingEntity.setStatus(EnStatus.I);

        try {
            this.ocorrenciaAnimalInterface.save(existingEntity);
            return "ocorrência animal excluída com sucesso";
        } catch (Exception e) {
            log.error("Erro ao excluir a ocorrência animal Id: {}", e.getMessage(), e);
            return "erro ao excluir ocorrência animal";
        }
    }
}
