package br.com.gado.application.services;

import br.com.gado.application.dto.SetorDto;
import br.com.gado.domain.entities.ESetor;
import br.com.gado.infrastructure.persistence.repositories.ISetor;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SSetor {

    @Autowired
    private ISetor setorInterface;

    @Autowired
    private ModelMapper modelMapper;

    public SetorDto procuraPorId(Long id) {
        ESetor setor = setorInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("nenhum setor encontrado"));
        return modelMapper.map(setor, SetorDto.class);
    }

    @Transactional
    public SetorDto cadastra(SetorDto dto) {
        ESetor setor = modelMapper.map(dto, ESetor.class);
        ESetor setorSalvo = setorInterface.save(setor);
        return modelMapper.map(setorSalvo, SetorDto.class);
    }

    @Transactional
    public String deleta(Long id) {
        boolean setor = setorInterface.existsById(id);
        if (!setor) {
            return "nenhum setor encontrado";
        }

        try {
            setorInterface.deleteById(id);
            return "setor deletado";
        } catch (Exception e) {
            return "nÃ£o foi possÃ­vel deletar porque o setor estÃ¡ vinculado com outras entidades";
        }
    }

    @Transactional
    public SetorDto altera(Long id, SetorDto dto) {
        ESetor setor = setorInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("nenhum setor encontrado"));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, setor);
        ESetor setorAtualizado = setorInterface.save(setor);
        return modelMapper.map(setorAtualizado, SetorDto.class);
    }
}
