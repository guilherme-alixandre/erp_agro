package br.com.gado.application.services;

import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.dto.loteDto.LoteCadastroDto;
import br.com.gado.dto.loteDto.LotePutDto;
import br.com.gado.infrastructure.persistence.repositories.ILote;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SLote {

    private final ILote loteInterface;
    private final IUsuario usuarioInterface;

    public SLote(ILote loteInterface, IUsuario usuarioInterface){
        this.loteInterface = loteInterface;
        this.usuarioInterface = usuarioInterface;
    }


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
        ELote lote = new ELote();

        Optional<EUsuario> usuarioOptional = usuarioInterface.findById(dto.getUsuario_id());
        if(usuarioOptional.isEmpty()){
            return "Nenhum usuário com esse id foi encontrado";
        }

        lote.setDescricao(dto.getDescricao());
        lote.setRacaPredominante(dto.getRacaPredominante());
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
        if(dto.getDescricao() != null){
            lote.setDescricao(dto.getDescricao());
        }

        if(dto.getRacaPredominante() != null){
            lote.setRacaPredominante(dto.getRacaPredominante());
        }

        loteInterface.save(lote);
        return "lote alterado com sucesso";
    }

}
