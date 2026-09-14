package br.com.gado.services;

import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalAtualizacaoDto;
import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalCadastroDto;
import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalRespostaDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EOcorrenciaAnimal;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.IOcorrenciaAnimal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SOcorrenciaAnimal {
    private final IOcorrenciaAnimal ocorrenciaAnimalInterface;
    private final IAnimal animalInterface;

    public SOcorrenciaAnimal(IOcorrenciaAnimal correnciaAnimalInterface, IAnimal animalInterface) {
        this.ocorrenciaAnimalInterface = correnciaAnimalInterface;
        this.animalInterface = animalInterface;
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

    public OcorrenciaAnimalRespostaDto atualizarOcorrencia(Long ocorrenciaAnimalId, OcorrenciaAnimalAtualizacaoDto dto) {
        EOcorrenciaAnimal ocorrencia = ocorrenciaAnimalInterface
                .findByIdAndStatus(ocorrenciaAnimalId, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Ocorrência não encontrada."));

        ocorrencia.setTipoOcorrencia(dto.getTipoOcorrencia());
        ocorrencia.setDataOcorrencia(dto.getDataOcorrencia());
        ocorrencia.setObservacao(dto.getObservacao());

        return toRespostaDto(ocorrenciaAnimalInterface.save(ocorrencia));
    }

    public void excluirOcorrencia(Long ocorrenciaAnimalId) {
        EOcorrenciaAnimal ocorrencia = ocorrenciaAnimalInterface
                .findByIdAndStatus(ocorrenciaAnimalId, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Ocorrência não encontrada."));

        ocorrencia.setStatus(EnStatus.I);
        ocorrenciaAnimalInterface.save(ocorrencia);
    }

    private OcorrenciaAnimalRespostaDto toRespostaDto(EOcorrenciaAnimal ocorrencia) {
        OcorrenciaAnimalRespostaDto dto = new OcorrenciaAnimalRespostaDto();
        dto.setId(ocorrencia.getId());
        dto.setTipoOcorrencia(ocorrencia.getTipoOcorrencia());
        dto.setDataOcorrencia(ocorrencia.getDataOcorrencia());
        dto.setObservacao(ocorrencia.getObservacao());
        return dto;
    }
}
