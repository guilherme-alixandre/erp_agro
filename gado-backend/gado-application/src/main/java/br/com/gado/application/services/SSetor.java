package br.com.gado.application.services;

import br.com.gado.domain.entities.ESetor;
import br.com.gado.application.dto.SetorDto;
import br.com.gado.infrastructure.persistence.repositories.ISetor;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SSetor {

    @Autowired
    private ISetor setorInterface;

    @Autowired
    private ModelMapper modelMapper;


    public Map<String, Object> procuraPorId(Long id){
        Optional<ESetor> setorOptional = setorInterface.findById(id);
        Map<String, Object> response = new HashMap<>();

        if(setorOptional.isEmpty()){
            response.put("Erro", "nenhum setor encontrado");
        }
        else {
            response.put("Setor", setorOptional.get());
        }

        return response;
    }

    @Transactional
    public String cadastra(SetorDto dto){
        ESetor setor = modelMapper.map(dto, ESetor.class);
        setorInterface.save(setor);
        return "Setor cadastrado com sucesso";
    }

    @Transactional
    public String deleta(Long id){
        boolean setor = setorInterface.existsById(id);
        if(!setor){
            return "nenhum setor encontrado";
        }

        try{
            setorInterface.deleteById(id);
            return "setor deletado";
        } catch (Exception e) {
            return "não foi possível deletar porque o setor está vinculado com outras entidades";
        }
    }

    @Transactional
    public String altera(Long id, SetorDto dto){
        Optional<ESetor> setorOptional = setorInterface.findById(id);
        if(setorOptional.isEmpty()){
            return "nenum setor encontrado";
        }

        ESetor setor = setorOptional.get();
        modelMapper.map(dto, setor);
        setorInterface.save(setor);
        return "setor alterado com sucesso";
    }
}
