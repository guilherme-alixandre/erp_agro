package br.com.gado.services;

import br.com.gado.dto.InsumoDto;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.EParceiro;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.IParceiro;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SInsumo {

    @Autowired
    private IInsumo insumoInterface;

    @Autowired
    private IParceiro parceiroInterface;

    @Autowired
    private ModelMapper modelMapper;

    public InsumoDto buscaPorId(Long id) {
        EInsumo insumo = insumoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Insumo não encontrado"));
        return modelMapper.map(insumo, InsumoDto.class);
    }

    @Transactional
    public InsumoDto cadastraInsumo(InsumoDto dto) {
        EInsumo insumo = modelMapper.map(dto, EInsumo.class);

        EParceiro parceiro = parceiroInterface.findById(dto.getParceiro_id())
                .orElseThrow(() -> new EntityNotFoundException("Id do fornecedor não encontrado"));

        insumo.setParceiro(parceiro);

        EInsumo insumoSalvo = insumoInterface.save(insumo);
        return modelMapper.map(insumoSalvo, InsumoDto.class);
    }

    @Transactional
    public String deletaInsumo(Long id) {
        if (insumoInterface.findById(id).isEmpty()) {
            return "nenhum insumo com esse id foi encontrado";
        }

        insumoInterface.deleteById(id);
        return "insumo deletado com sucesso";
    }

    @Transactional
    public InsumoDto alteraInsumo(Long id, InsumoDto dto) {
        EInsumo insumo = insumoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("nenhum insumo encontrado com esse id"));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, insumo);
        EInsumo insumoAtualizado = insumoInterface.save(insumo);
        return modelMapper.map(insumoAtualizado, InsumoDto.class);
    }
}
