package br.com.gado.application.services;

import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.application.dto.loteDto.LoteCadastroDto;
import br.com.gado.application.dto.loteDto.LotePutDto;
import br.com.gado.infrastructure.persistence.repositories.ILote;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SLote {

    @Autowired
    private ILote loteInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;


    public Map<String, Object> buscaPorid(Long id){
        Optional<ELote> loteOptional = loteInterface.findById(id);
        Map<String, Object> response = new HashMap<>();

        if(loteOptional.isEmpty()){
            response.put("mensagem", "nenhum lote encontrado");
        }
        else {
            ELote lote = loteOptional.get();
            response.put("lote", lote);
        }

        return response;
    }

    @Transactional
    public String cadastra(LoteCadastroDto dto){
        Optional<EUsuario> usuarioOptional = usuarioInterface.findById(dto.getUsuario_id());
        if(usuarioOptional.isEmpty()){
            return "Nenhum usuário com esse id foi encontrado";
        }

        ELote lote = modelMapper.map(dto, ELote.class);
        lote.setUsuario(usuarioOptional.get());
        loteInterface.save(lote);
        return "lote criado com sucesso";
    }

    @Transactional
    public String deleta(Long id){

        boolean existe = loteInterface.existsById(id);
        if(!existe){
            return "nenhum lote foi encontrado";
        }

        try{
            loteInterface.deleteById(id);
            return "lote deletado com sucesso";
        } catch (Exception e){
            return "Erro: esse lote possui transações vinculadas e não pode ser excluido";
        }
    }

    @Transactional
    public String altera(Long id, LotePutDto dto){

        Optional<ELote> loteOptional = loteInterface.findById(id);
        if(loteOptional.isEmpty()){
            return "nenhum lote encontrado";
        }

        ELote lote = loteOptional.get();
        modelMapper.map(dto, lote);
        loteInterface.save(lote);
        return "lote alterado com sucesso";
    }

}
