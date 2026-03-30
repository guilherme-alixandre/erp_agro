package br.com.gado.application.services;

import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.EOcorrenciaAnimal;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.application.dto.OcorrenciaAnimalDTO;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.IOcorrenciaAnimal;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SOcorrenciaAnimal {
    private final IOcorrenciaAnimal ocorrenciaAnimalInterface;
    private final IAnimal animalInterface;
    private static final Logger log = LoggerFactory.getLogger(SOcorrenciaAnimal.class);
    private final ModelMapper modelMapper;


    public SOcorrenciaAnimal(IOcorrenciaAnimal correnciaAnimalInterface, IAnimal animalInterface, ModelMapper modelMapper) {
        this.ocorrenciaAnimalInterface = correnciaAnimalInterface;
        this.animalInterface = animalInterface;
        this.modelMapper = modelMapper;
    }

    public OcorrenciaAnimalDTO criarOcorrenciaAnimal(OcorrenciaAnimalDTO ocorrenciaAnimalDto) {

        EAnimal existingAnimal = this.animalInterface
                .findByCodigoBrinco(ocorrenciaAnimalDto.getIdAnimal().getCodigoBrinco())
                .orElseThrow(EntityNotFoundException::new);

        EOcorrenciaAnimal novaOcorrenciaAnimal = new EOcorrenciaAnimal();

        novaOcorrenciaAnimal.setTipoOcorrencia(ocorrenciaAnimalDto.getTipoOcorrencia());
        novaOcorrenciaAnimal.setDataOcorrencia(ocorrenciaAnimalDto.getDataOcorrencia());
        novaOcorrenciaAnimal.setObservacao(ocorrenciaAnimalDto.getObservacao());
        novaOcorrenciaAnimal.setIdAnimal(existingAnimal);

        try{
            EOcorrenciaAnimal ocorrenciaAnimalSalva = this.ocorrenciaAnimalInterface.save(novaOcorrenciaAnimal);
            return this.modelMapper.map(ocorrenciaAnimalSalva, OcorrenciaAnimalDTO.class);
        } catch (Exception e){
            log.error("Erro ao criar a correncia animal Id: {}", e.getMessage(), e);
            throw e;
        }
    }

    public OcorrenciaAnimalDTO encontrarOcorrenciaAnimalPorId(OcorrenciaAnimalDTO ocorrenciaAnimalDto) {
        EOcorrenciaAnimal existingEntitye = this.ocorrenciaAnimalInterface
                .findById(ocorrenciaAnimalDto.getId())
                .orElseThrow(EntityNotFoundException::new);

        if(existingEntitye != null){
            return this.modelMapper.map(existingEntitye, OcorrenciaAnimalDTO.class);
        } else {
            log.error("Erro, Ocorrencia não encontrada para o id {}", ocorrenciaAnimalDto.getId());
            throw new EntityNotFoundException();
        }
    }

    public OcorrenciaAnimalDTO atualizarOcorrenciaAnimal(OcorrenciaAnimalDTO ocorrenciaAnimalDto) {
        EOcorrenciaAnimal existingEntitye = this.ocorrenciaAnimalInterface
                .findById(ocorrenciaAnimalDto.getId())
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);

        this.modelMapper.map(ocorrenciaAnimalDto, existingEntitye);

        try{
            EOcorrenciaAnimal ocorenciaAnimalAtualizada = this.ocorrenciaAnimalInterface.save(existingEntitye);
            return modelMapper.map(ocorenciaAnimalAtualizada, OcorrenciaAnimalDTO.class);
        } catch (Exception e){
            log.error("Erro ao atualizar a correncia animal Id: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean excluirOcorrenciaAnimal(Long ocorrenciaAnimalId) {
        EOcorrenciaAnimal existingEntitye = this.ocorrenciaAnimalInterface
                .findById(ocorrenciaAnimalId)
                .orElseThrow(EntityNotFoundException::new);

        existingEntitye.setStatus(EnStatus.I);

        try {
            this.ocorrenciaAnimalInterface.save(existingEntitye);
            return true;
        } catch (Exception e){
            log.error("Erro ao excluir a correncia animal Id: {}", e.getMessage(), e);
            return false;
        }
    }
}
