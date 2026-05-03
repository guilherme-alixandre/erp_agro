package br.com.gado.application.services;

import br.com.gado.application.dto.AnimalDto;
import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SAnimal {

    @Autowired
    private IAnimal animalInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;

    public AnimalDto buscarPorBrinco(String brinco) {
        EAnimal animal = animalInterface.findByCodigoBrincoAndStatus(brinco, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("animal não encontrado"));
        return modelMapper.map(animal, AnimalDto.class);
    }

    public ArrayList<AnimalDto> buscarTodosAnimais() {
        ArrayList<EAnimal> listaAnimais = animalInterface.findAllByStatus(EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("nenhum animal encontrado"));

        return listaAnimais.stream()
                .map(animal -> modelMapper.map(animal, AnimalDto.class))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    public AnimalDto cadastraAnimal(String email, AnimalDto animalDto) {
        EAnimal novoAnimal = modelMapper.map(animalDto, EAnimal.class);

        EUsuario usuario = usuarioInterface.findByEmailAndStatus(email, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        novoAnimal.setUsuario(usuario);
        EAnimal animalSalvo = animalInterface.save(novoAnimal);
        return modelMapper.map(animalSalvo, AnimalDto.class);
    }

    @Transactional
    public String deletaAnimal(String brinco) {
        EAnimal animal = animalInterface.findByCodigoBrincoAndStatus(brinco, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("animal não encontrado"));

        animal.setStatus(EnStatus.I);

        animalInterface.save(animal);
        return "animal deletado com sucesso";
    }

    @Transactional
    public AnimalDto alteraAnimal(String brinco, AnimalDto dto) {
        EAnimal animal = animalInterface.findByCodigoBrincoAndStatus(brinco, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("animal não encontrado"));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, animal);

        EAnimal animalAtualizado = animalInterface.save(animal);
        return modelMapper.map(animalAtualizado, AnimalDto.class);
    }
}