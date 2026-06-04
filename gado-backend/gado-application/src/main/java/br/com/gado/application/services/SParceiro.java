package br.com.gado.application.services;

import br.com.gado.application.dto.parcerioDto.ParceiroCadastroDto;
import br.com.gado.application.dto.parcerioDto.ParceiroDto;
import br.com.gado.application.dto.parcerioDto.ParceiroPutDto;
import br.com.gado.domain.entities.EParceiro;
import br.com.gado.infrastructure.persistence.repositories.IParceiro;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SParceiro {

    @Autowired
    private IParceiro parceiroInterface;

    @Autowired
    private ModelMapper modelMapper;

    public ParceiroDto buscaPorCPF_CNPJ(String cpfCnpj) {
        EParceiro parceiro = parceiroInterface.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new EntityNotFoundException("nenhum parceiro encontrado com esse cpf/cnpj"));
        return modelMapper.map(parceiro, ParceiroDto.class);
    }

    @Transactional
    public ParceiroDto cadastra(ParceiroCadastroDto dto) {
        boolean existe = parceiroInterface.existsByCpfCnpj(dto.getCPF_CNPJ());
        if (existe) {
            throw new IllegalArgumentException("Parceiro já existe no sistema");
        }

        EParceiro parceiro = modelMapper.map(dto, EParceiro.class);
        parceiro.setDataCadastro(LocalDateTime.now());

        EParceiro parceiroSalvo = parceiroInterface.save(parceiro);
        return modelMapper.map(parceiroSalvo, ParceiroDto.class);
    }

    @Transactional
    public String deleta(String cpfCnpj) {
        if (!parceiroInterface.existsByCpfCnpj(cpfCnpj)) {
            return "Esse cpf/cnpj não existe no banco de dados";
        }

        try {
            parceiroInterface.deleteByCpfCnpj(cpfCnpj);
            return "Parceiro deletado com sucesso";
        } catch (Exception e) {
            return "Não é possível excluir ele pois ele possui vinculos com outras entidades";
        }
    }

    @Transactional
    public ParceiroDto altera(String cpfCnpj, ParceiroPutDto dto) {
        EParceiro parceiro = parceiroInterface.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não existe no sistema"));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, parceiro);
        EParceiro parceiroAtualizado = parceiroInterface.save(parceiro);
        return modelMapper.map(parceiroAtualizado, ParceiroDto.class);
    }
}
