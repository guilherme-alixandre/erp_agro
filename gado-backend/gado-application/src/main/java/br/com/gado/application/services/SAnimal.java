package br.com.gado.application.services;

import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.application.dto.AnimalDto;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SAnimal {

    @Autowired
    private IAnimal animalInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;

    public Map<String, Object> buscarPorBrinco(String brinco){
        Map<String, Object> response = new HashMap<>();
        Optional<EAnimal> animal = animalInterface.findByCodigoBrinco(brinco);

        if(animal.isPresent()) {
            response.put("mensagem", animal.get());
        } else {
            response.put("mensagem", "animal não encontrado");
        }

        return response;
    }

    @Transactional
    public String cadastraAnimal(String email, AnimalDto animal){
        try{
            EAnimal novoAnimal = modelMapper.map(animal, EAnimal.class);

            EUsuario usuario = usuarioInterface.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            novoAnimal.setUsuario(usuario);
            animalInterface.save(novoAnimal);

            return "animal cadastrado";

        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }

    @Transactional
    public String deletaAnimal(String brinco){
        Optional<EAnimal> animal = animalInterface.findByCodigoBrinco(brinco);

        if(animal.isPresent()){
            animalInterface.deleteByCodigoBrinco(brinco);
            return "animal deletado";
        }
        return "animal não encontrado";

    }

    @Transactional
    public String alteraAnimal(String brinco, AnimalDto dto){
        Optional<EAnimal> animalOptional = animalInterface.findByCodigoBrinco(brinco);

        if(animalOptional.isEmpty()){
            return "animal não encontrado";
        }

        EAnimal animal = animalOptional.get();
        modelMapper.map(dto, animal);
        animalInterface.save(animal);
        return "animal atualizado";
    }
}
