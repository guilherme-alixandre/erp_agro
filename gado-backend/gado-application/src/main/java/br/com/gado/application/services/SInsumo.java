package br.com.gado.application.services;

import br.com.gado.domain.entities.EInsumo;
import br.com.gado.domain.entities.EParceiro;
import br.com.gado.application.dto.InsumoDto;
import br.com.gado.infrastructure.persistence.repositories.IInsumo;
import br.com.gado.infrastructure.persistence.repositories.IParceiro;
import javax.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SInsumo {

    @Autowired
    private IInsumo insumoInterface;

    @Autowired
    private IParceiro parceiroInterface;

    @Autowired
    private ModelMapper modelMapper;


    public Map<String, Object> buscaPorId(Long id){
        Map<String, Object> response = new HashMap<>();
        Optional<EInsumo> insumo = insumoInterface.findById(id);

        if(insumo.isEmpty()){
            response.put("Erro", "Insumo não encontrado");
        }
        else {
            response.put("insumo", insumo.get());
        }

        return response;
    }

    @Transactional
    public String cadastraInsumo(InsumoDto dto){
        EInsumo insumo = new EInsumo();

        insumo.setNome(dto.getNome());
        insumo.setEstoqueMinimo(dto.getEstoqueMinimo());
        insumo.setSaldoAtual(dto.getSaldoAtual());

        Optional<EParceiro> parceiroOptional = parceiroInterface.findById(dto.getParceiro_id());
        if(parceiroOptional.isEmpty()){
            return "Id do forcenedor não encontrado";
        }
        insumo.setParceiro(parceiroOptional.get());
        insumo.setTipo(dto.getTipo());

        insumoInterface.save(insumo);
        return "insumo cadastrado";
    }

    @Transactional
    public String deletaInsumo(Long id){
        Optional<EInsumo> insumoOptional = insumoInterface.findById(id);

        if(insumoOptional.isEmpty()){
            return "nenhum insumo com esse id foi encontrado";
        }

        insumoInterface.deleteById(id);
        return "insumo deletado com sucesso";
    }

    @Transactional
    public String alteraInsumo(Long id, InsumoDto dto){
        Optional<EInsumo> insumoOptional = insumoInterface.findById(id);

        if(insumoOptional.isEmpty()){
            return "nenhum insumo encontrado com esse id";
        }

        EInsumo insumo = insumoOptional.get();
        modelMapper.map(dto, insumo);

        insumoInterface.save(insumo);
        return "insumo alterado com sucesso";
    }

}
