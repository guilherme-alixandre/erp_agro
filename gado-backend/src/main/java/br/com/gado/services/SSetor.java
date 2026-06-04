package br.com.gado.services;

import br.com.gado.dto.SetorDto;
import br.com.gado.entities.ESetor;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.ISetor;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SSetor {

    @Autowired
    private ISetor setorInterface;

    @Autowired
    private ModelMapper modelMapper;

    public SetorDto procuraPorId(Long id) {
        ESetor setor = setorInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("nenhum setor encontrado"));
        return modelMapper.map(setor, SetorDto.class);
    }

    @Transactional(readOnly = true)
    public ArrayList<SetorDto> buscarTodos() {
        ArrayList<ESetor> setores = setorInterface.findAllByStatus(EnStatus.A);
        if (setores.isEmpty()) {
            log.warn("Nenhum setor ativo cadastrado.");
            return new ArrayList<>();
        }
        return setores.stream()
                .map(setor -> modelMapper.map(setor, SetorDto.class))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    public SetorDto cadastra(SetorDto dto) {
        ESetor setor = modelMapper.map(dto, ESetor.class);
        ESetor setorSalvo = setorInterface.save(setor);
        return modelMapper.map(setorSalvo, SetorDto.class);
    }

    @Transactional
    public void deleta(Long id) {
        ESetor setor = setorInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado ou inativo"));

        setor.setStatus(EnStatus.I);
        setorInterface.save(setor);
    }

    @Transactional
    public SetorDto altera(Long id, SetorDto dto) {
        ESetor setor = setorInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Setor encontrado"));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        dto.setId(id);
        modelMapper.map(dto, setor);

        ESetor setorAtualizado = setorInterface.save(setor);
        return modelMapper.map(setorAtualizado, SetorDto.class);
    }
}
