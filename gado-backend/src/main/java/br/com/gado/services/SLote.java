package br.com.gado.services;

import br.com.gado.dto.loteDto.LoteCadastroDto;
import br.com.gado.dto.loteDto.LoteDto;
import br.com.gado.dto.loteDto.LotePutDto;
import br.com.gado.entities.ELote;
import br.com.gado.entities.EUsuario;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SLote {

    @Autowired
    private ILote loteInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;

    public LoteDto buscaPorId(Long id) {
        ELote lote = loteInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("nenhum lote encontrado"));
        return modelMapper.map(lote, LoteDto.class);
    }

    @Transactional
    public LoteDto cadastra(LoteCadastroDto dto) {
        EUsuario usuario = usuarioInterface.findById(dto.getUsuario_id())
                .orElseThrow(() -> new EntityNotFoundException("Nenhum usuário com esse id foi encontrado"));

        ELote lote = modelMapper.map(dto, ELote.class);
        lote.setUsuario(usuario);

        ELote loteSalvo = loteInterface.save(lote);
        return modelMapper.map(loteSalvo, LoteDto.class);
    }

    @Transactional
    public String deleta(Long id) {
        // só refatorei esse if pra ficar menor, não mudei a lógica
        if(!loteInterface.existsById(id)){
            return "nenhum lote foi encontrado";
        }

        try {
            loteInterface.deleteById(id);
            return "lote deletado com sucesso";
        } catch (Exception e) {
            return "Erro: esse lote possui transações vinculadas e não pode ser excluido";
        }
    }

    @Transactional
    public LoteDto altera(Long id, LotePutDto dto) {
        ELote lote = loteInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("nenhum lote encontrado"));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, lote);
        ELote loteAtualizado = loteInterface.save(lote);
        return modelMapper.map(loteAtualizado, LoteDto.class);
    }
}
