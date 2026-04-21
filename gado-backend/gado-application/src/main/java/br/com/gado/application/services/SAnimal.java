package br.com.gado.application.services;

import br.com.gado.application.dto.AnimalDto;
import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SAnimal {

    @Autowired
    private IAnimal animalInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;

    public AnimalDto buscarPorBrinco(String brinco) {
        EAnimal animal = animalInterface.findByCodigoBrinco(brinco)
                .orElseThrow(() -> new EntityNotFoundException("animal nÃ£o encontrado"));
        return modelMapper.map(animal, AnimalDto.class);
    }

    @Transactional
    public AnimalDto cadastraAnimal(String email, AnimalDto animalDto) {
        EAnimal novoAnimal = modelMapper.map(animalDto, EAnimal.class);

        EUsuario usuario = usuarioInterface.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("UsuÃ¡rio nÃ£o encontrado"));

        novoAnimal.setUsuario(usuario);
        EAnimal animalSalvo = animalInterface.save(novoAnimal);

        return modelMapper.map(animalSalvo, AnimalDto.class);
    }

    @Transactional
    public String deletaAnimal(String brinco) {
        if (animalInterface.findByCodigoBrinco(brinco).isPresent()) {
            animalInterface.deleteByCodigoBrinco(brinco);
            return "animal deletado";
        }
        return "animal nÃ£o encontrado";
    }

    @Transactional
    public AnimalDto alteraAnimal(String brinco, AnimalDto dto) {
        EAnimal animal = animalInterface.findByCodigoBrinco(brinco)
                .orElseThrow(() -> new EntityNotFoundException("animal nÃ£o encontrado"));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, animal);
        EAnimal animalAtualizado = animalInterface.save(animal);
        return modelMapper.map(animalAtualizado, AnimalDto.class);
    }
}
