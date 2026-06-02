package br.com.gado.application.services;

import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.application.dto.loteDto.LoteCadastroDto;
import br.com.gado.application.dto.loteDto.LoteDto;
import br.com.gado.application.dto.loteDto.LotePutDto;
import br.com.gado.application.dto.usuarioDto.UsuarioDto;
import br.com.gado.infrastructure.persistence.repositories.ILote;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import javax.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SLote {

    @Autowired
    private ILote loteInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;


    public LoteDto buscaPorid(Long id){
        ELote lote = loteInterface.findById(id)
                .orElseThrow(() -> new RuntimeException("Lote não encontrado"));
        return modelMapper.map(lote, LoteDto.class);
    }

    @Transactional
    public Map<String, Object> cadastra(LoteCadastroDto dto){
        Optional<EUsuario> usuarioOptional = usuarioInterface.findById(dto.getUsuario_id());
        if(usuarioOptional.isEmpty()){
            throw new RuntimeException("Nenhum usuário com esse id foi encontrado");
        }

        ELote lote = modelMapper.map(dto, ELote.class);
        lote.setUsuario(usuarioOptional.get());
        loteInterface.save(lote);

        Map<String, Object> response = new HashMap<>();
        response.put("lote", modelMapper.map(lote, LoteDto.class));
        return response;
    }

    @Transactional
    public void deleta(Long id){

        boolean existe = loteInterface.existsById(id);
        if(!existe){
            throw new RuntimeException("Nenhum lote foi encontrado");
        }

        try{
            loteInterface.deleteById(id);
        } catch (Exception e){
            throw new RuntimeException("Erro: esse lote possui transações vinculadas e não pode ser excluido");
        }
    }

    public List<LoteDto> buscaLotes(Long id, String descricao) {
        List<ELote> lotes = loteInterface.findByIdAndDescricaoPartialMatch(id, descricao);
        return lotes.stream()
                .map(lote -> modelMapper.map(lote, LoteDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> altera(Long id, LotePutDto dto){

        Optional<ELote> loteOptional = loteInterface.findById(id);
        if(loteOptional.isEmpty()){
            throw new RuntimeException("Nenhum lote encontrado");
        }

        ELote lote = loteOptional.get();
        modelMapper.map(dto, lote);
        loteInterface.save(lote);

        Map<String, Object> response = new HashMap<>();
        response.put("lote", modelMapper.map(lote, LoteDto.class));
        return response;
    }

    public String exportAllLotesToCsv() {
        List<ELote> lotes = loteInterface.findAll();

        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID,Descrição,Raça Predominante,Status,Criado Em,Usuário ID,Usuário Nome\n");

        for (ELote lote : lotes) {
            csvContent.append(lote.getId()).append(",");
            csvContent.append(escapeCsv(lote.getDescricao())).append(",");
            csvContent.append(escapeCsv(lote.getRacaPredominante())).append(",");
            csvContent.append(lote.getStatus()).append(",");
            csvContent.append(lote.getCreatedAt()).append(",");
            csvContent.append(lote.getUsuario() != null ? lote.getUsuario().getId() : "").append(",");
            csvContent.append(lote.getUsuario() != null ? escapeCsv(lote.getUsuario().getNome()) : "");
            csvContent.append("\n");
        }
        return csvContent.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\""); // Escape double quotes
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

}
