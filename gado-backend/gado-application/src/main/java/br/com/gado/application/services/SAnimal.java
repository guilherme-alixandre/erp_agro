package br.com.gado.application.services;

import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.dto.AnimalDto;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SAnimal {
    private final IAnimal animalInterface;
    private final IUsuario usuarioInterface;

    public SAnimal(IAnimal animalInterface, IUsuario usuarioInterface){
        this.animalInterface = animalInterface;
        this.usuarioInterface = usuarioInterface;
    }


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
    public String cadastraAnimal(AnimalDto animal){
        try{
            EAnimal novoAnimal = new EAnimal();

            novoAnimal.setCodigoBrinco(animal.getCodigoBrinco());
            novoAnimal.setNome(animal.getNome());
            novoAnimal.setDataNascimento(animal.getDataNascimento());
            novoAnimal.setPesoAtual(animal.getPesoAtual());
            novoAnimal.setRaca(animal.getRaca());
            novoAnimal.setCor(animal.getCor());
            novoAnimal.setTamanho(animal.getTamanho());
            novoAnimal.setSexo(animal.getSexo());
            novoAnimal.setStatus(animal.getStatus());

            EUsuario usuario = usuarioInterface.findById(animal.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            novoAnimal.setUsuarioId(usuario);

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

        // as alterações
        // ain, pra que isso? se eu não fizer isso e fazer do jeito normal
        // o que eu não passar vira null, assim salva o que eu não passar
        EAnimal animal = animalOptional.get();

        if(dto.getRaca() != null){
            animal.setRaca(dto.getRaca());
        }

        if(dto.getNome() != null){
            animal.setNome(dto.getNome());
        }

        if(dto.getSexo() != null){
            animal.setSexo(dto.getSexo());
        }

        if(dto.getCor() != null){
            animal.setCor(dto.getCor());
        }

        if(dto.getDataNascimento() != null){
            animal.setDataNascimento(dto.getDataNascimento());
        }

        if(dto.getPesoAtual() != null){
            animal.setPesoAtual(dto.getPesoAtual());
        }

        if(dto.getTamanho() != null){
            animal.setTamanho(dto.getTamanho());
        }

        if(dto.getStatus() != null){
            animal.setStatus(dto.getStatus());
        }

        animalInterface.save(animal);
        return "animal atualizado";
    }
}
