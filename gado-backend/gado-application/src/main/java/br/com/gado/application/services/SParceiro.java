package br.com.gado.application.services;

import br.com.gado.domain.entities.EParceiro;
import br.com.gado.dto.parcerioDto.ParceiroCadastroDto;
import br.com.gado.dto.parcerioDto.ParceiroPutDto;
import br.com.gado.infrastructure.persistence.repositories.IParceiro;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SParceiro {

    private final IParceiro parceiroInterface;

    public SParceiro(IParceiro parceiroInterface){
        this.parceiroInterface = parceiroInterface;
    }


    public Map<String, Object> buscaPorCPF_CNPJ(String cpf_cnpj){
        Map<String, Object> response = new HashMap<>();
        Optional<EParceiro> parceiroOptional = parceiroInterface.findByCPF_CNPJ(cpf_cnpj);

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
        boolean existe = parceiroInterface.existsByCPF_CNPJ(dto.getCPF_CNPJ());
        if(existe){
            return "Parceiro já existe no sistema";
        }

        EParceiro parceiro = new EParceiro();
        parceiro.setNome(dto.getNome());
        parceiro.setTipo(dto.getTipo());
        parceiro.setEndereco(dto.getEndereco());
        parceiro.setCPF_CNPJ(dto.getCPF_CNPJ());
        parceiro.setTelefone(dto.getTelefone());
        parceiro.setDataCadastro(LocalDateTime.now());

        parceiroInterface.save(parceiro);
        return "Parceiro criado com sucesso";
    }

    @Transactional
    public String deleta(String cpf_cnpj){
        if(!parceiroInterface.existsByCPF_CNPJ(cpf_cnpj)){
            return "Esse cpf/cnpj não existe no banco de dados";
        }

        try{
            parceiroInterface.deleteByCPF_CNPJ(cpf_cnpj);
            return "Parceiro deletado com sucesso";
        }
        catch (Exception e) {
            return "Não é possível excluir ele pois ele possui vinculos com outras entidades";
        }
    }

    @Transactional
    public String altera(String cpf_cnpj, ParceiroPutDto dto){
        Optional<EParceiro> parceiroOptional = parceiroInterface.findByCPF_CNPJ(cpf_cnpj);
        if(parceiroOptional.isEmpty()){
            return "Parceiro não existe no sistema";
        }

        EParceiro parceiro = parceiroOptional.get();
        if(dto.getNome() != null){
            parceiro.setNome(dto.getNome());
        }

        if(dto.getTelefone() != null){
            parceiro.setTelefone(dto.getTelefone());
        }

        if(dto.getTipo() != null){
            parceiro.setTipo(dto.getTipo());
        }

        if(dto.getEndereco() != null){
            parceiro.setEndereco(dto.getEndereco());
        }

        parceiroInterface.save(parceiro);
        return "Parceiro atualizado";
    }

}
