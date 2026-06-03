package br.com.gado.services;

import br.com.gado.dto.AnimalDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
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
        Optional<ArrayList<EAnimal>> animais = animalInterface.findAllByStatus(EnStatus.A);
        if (animais.isEmpty())
            return new ArrayList<>();

        return animais.stream()
                .map(animal -> modelMapper.map(animal, AnimalDto.class))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    public AnimalDto cadastraAnimal(String email, AnimalDto animalDto) throws Exception {
        EAnimal novoAnimal = modelMapper.map(animalDto, EAnimal.class);


        if(animalInterface.existsByCodigoBrincoAndStatus(novoAnimal.getCodigoBrinco(), EnStatus.A))
            throw new Exception("Código do brinco deve ser único");

        EUsuario usuario = usuarioInterface.findByEmailAndStatus(email, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        novoAnimal.setUsuario(usuario);
        EAnimal animalSalvo = animalInterface.save(novoAnimal);

        return modelMapper.map(animalSalvo, AnimalDto.class);
    }

    @Transactional
    public String deletaAnimal(String brinco) {
        Optional<EAnimal> animalOptional = animalInterface.findByCodigoBrincoAndStatus(brinco, EnStatus.A);

        if(animalOptional.isEmpty())
            return "animal não encontrado";

        EAnimal animal = animalOptional.get();
        animal.setStatus(EnStatus.I);
        return "Animal deletado com sucesso";
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
