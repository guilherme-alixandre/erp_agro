package br.com.gado.application.services;

import br.com.gado.domain.entities.EParceiro;
import br.com.gado.application.dto.parcerioDto.ParceiroCadastroDto;
import br.com.gado.application.dto.parcerioDto.ParceiroPutDto;
import br.com.gado.infrastructure.persistence.repositories.IParceiro;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SParceiro {

    @Autowired
    private IParceiro parceiroInterface;

    @Autowired
    private ModelMapper modelMapper;


    public Map<String, Object> buscaPorCPF_CNPJ(String cpf_cnpj){
        Map<String, Object> response = new HashMap<>();
        Optional<EParceiro> parceiroOptional = parceiroInterface.findByCpfCnpj(cpf_cnpj);

        if(parceiroOptional.isEmpty()){
            response.put("Erro", "nenhum parceiro encontrado com esse cpf/cnpj");
        }
        else {
            response.put("parceiro", parceiroOptional.get());
        }

        return response;
    }

    @Transactional
    public String cadastra(ParceiroCadastroDto dto){
        boolean existe = parceiroInterface.existsByCpfCnpj(dto.getCPF_CNPJ());
        if(existe){
            return "Parceiro já existe no sistema";
        }

        EParceiro parceiro = modelMapper.map(dto, EParceiro.class);
        parceiro.setDataCadastro(LocalDateTime.now());

        parceiroInterface.save(parceiro);
        return "Parceiro criado com sucesso";
    }

    @Transactional
    public String deleta(String cpf_cnpj){
        if(!parceiroInterface.existsByCpfCnpj(cpf_cnpj)){
            return "Esse cpf/cnpj não existe no banco de dados";
        }

        try{
            parceiroInterface.deleteByCpfCnpj(cpf_cnpj);
            return "Parceiro deletado com sucesso";
        }
        catch (Exception e) {
            return "Não é possível excluir ele pois ele possui vinculos com outras entidades";
        }
    }

    @Transactional
    public String altera(String cpf_cnpj, ParceiroPutDto dto){
        Optional<EParceiro> parceiroOptional = parceiroInterface.findByCpfCnpj(cpf_cnpj);
        if(parceiroOptional.isEmpty()){
            return "Parceiro não existe no sistema";
        }

        EParceiro parceiro = parceiroOptional.get();
        modelMapper.map(dto, parceiro);

        parceiroInterface.save(parceiro);
        return "Parceiro atualizado";
    }

}
