package br.com.gado.application.services;

import br.com.gado.application.dto.AnimalDto;
import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.ELoteSetor;
import br.com.gado.domain.entities.ESetor;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.ILoteSetor;
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
    private ILoteSetor loteSetorInterface;

    @Autowired
    private ModelMapper modelMapper;

    public AnimalDto buscarPorBrinco(String brinco) {
        EAnimal animal = animalInterface.findByCodigoBrincoAndStatus(brinco, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("animal não encontrado"));
        return toDto(animal);
    }

    public ArrayList<AnimalDto> buscarTodosAnimais() {
        ArrayList<EAnimal> listaAnimais = animalInterface.findAllByStatus(EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("nenhum animal encontrado"));

        return listaAnimais.stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private AnimalDto toDto(EAnimal animal) {
        AnimalDto dto = modelMapper.map(animal, AnimalDto.class);
        if (animal.getUsuario() != null) {
            dto.setCriadoPorEmail(animal.getUsuario().getEmail());
        }
        return dto;
    }

    @Transactional
    public AnimalDto cadastraAnimal(String email, AnimalDto animalDto) {
        if (animalDto.getLoteSectorId() == null) {
            throw new IllegalArgumentException("O vínculo com um lote/setor é obrigatório ao cadastrar um animal.");
        }

        ELoteSetor loteSetor = loteSetorInterface.findById(animalDto.getLoteSectorId())
                .orElseThrow(() -> new EntityNotFoundException("Alocação de lote/setor não encontrada."));

        if (loteSetor.getLote().getStatus() != EnStatus.A) {
            throw new IllegalArgumentException("Não é possível vincular o animal a um lote inativo.");
        }

        ESetor setor = loteSetor.getSetor();
        if (loteSetor.getAnimais().size() >= setor.getCapacidadeMaxima()) {
            throw new IllegalArgumentException(
                    "Capacidade máxima do setor '" + setor.getNome() + "' atingida (" + setor.getCapacidadeMaxima() + " animais).");
        }

        EAnimal novoAnimal = modelMapper.map(animalDto, EAnimal.class);

        EUsuario usuario = usuarioInterface.findByEmailAndStatus(email, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        novoAnimal.setUsuario(usuario);
        EAnimal animalSalvo = animalInterface.save(novoAnimal);

        loteSetor.getAnimais().add(animalSalvo);
        loteSetorInterface.save(loteSetor);

        return toDto(animalSalvo);
    }

    @Transactional
    public String deletaAnimal(String emailUsuario, String brinco) {
        EAnimal animal = animalInterface.findByCodigoBrincoAndStatus(brinco, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("animal não encontrado"));

        if (emailUsuario != null && !emailUsuario.isBlank()) {
            EUsuario solicitante = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                    .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
            EnPerfilUsuario perfil = solicitante.getPerfil();
            if (perfil == EnPerfilUsuario.CUIDADOR || perfil == EnPerfilUsuario.CUIDADOR_CHEFE) {
                String dono = animal.getUsuario() != null ? animal.getUsuario().getEmail() : null;
                if (!emailUsuario.trim().equals(dono)) {
                    throw new IllegalArgumentException("Você só pode excluir animais que você cadastrou.");
                }
            }
        }

        animal.setStatus(EnStatus.I);
        animalInterface.save(animal);
        return "animal deletado com sucesso";
    }

    @Transactional
    public AnimalDto alteraAnimal(String emailUsuario, String brinco, AnimalDto dto) {
        EAnimal animal = animalInterface.findByCodigoBrincoAndStatus(brinco, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("animal não encontrado"));

        if (emailUsuario != null && !emailUsuario.isBlank()) {
            EUsuario solicitante = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                    .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
            if (solicitante.getPerfil() == EnPerfilUsuario.CUIDADOR) {
                String dono = animal.getUsuario() != null ? animal.getUsuario().getEmail() : null;
                if (!emailUsuario.trim().equals(dono)) {
                    throw new IllegalArgumentException("Você só pode editar animais que você cadastrou.");
                }
            }
        }

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, animal);

        EAnimal animalAtualizado = animalInterface.save(animal);
        return toDto(animalAtualizado);
    }
}