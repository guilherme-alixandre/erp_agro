package br.com.gado.application.services;

import br.com.gado.domain.entities.ESetor;
import br.com.gado.application.dto.setorDto.SetorCadastroDto;
import br.com.gado.application.dto.setorDto.SetorDto;
import br.com.gado.application.dto.setorDto.SetorPutDto;
import br.com.gado.application.dto.usuarioDto.UsuarioDto;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.com.gado.infrastructure.persistence.repositories.ISetor;
import javax.transaction.Transactional;
import org.springframework.data.domain.PageImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SSetor {

    @Autowired
    private ISetor setorInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;


    public SetorDto buscaPorId(Long id){
        ESetor setor = setorInterface.findById(id)
                .orElseThrow(() -> new RuntimeException("Setor não encontrado"));
        return modelMapper.map(setor, SetorDto.class);
    }

    @Transactional
    public Map<String, Object> cadastra(SetorCadastroDto dto){
        ESetor setor = new ESetor();
        setor.setDescricao(dto.getDescricao());
        setor.setCapacidadeMaxima(dto.getCapacidadeMaxima());
        setor.setMetaTexto(dto.getMetaTexto());
        setor.setMetaProducaoLeite(dto.getMetaProducaoLeite());
        setor.setMetaArrobaAbate(dto.getMetaArrobaAbate());
        setor.setSetor(dto.getSetor());

        if (dto.getUsuario_id() != null) {
            Optional<EUsuario> usuarioOptional = usuarioInterface.findById(dto.getUsuario_id());
            if(usuarioOptional.isEmpty()){
                throw new RuntimeException("Nenhum usuário com esse id foi encontrado");
            }
            setor.setUsuario(usuarioOptional.get());
        }

        setorInterface.save(setor);

        Map<String, Object> response = new HashMap<>();
        response.put("setor", modelMapper.map(setor, SetorDto.class));
        return response;
    }

    @Transactional
    public Map<String, String> deleta(Long id){
        boolean existe = setorInterface.existsById(id);
        if(!existe){
            throw new RuntimeException("Nenhum setor foi encontrado");
        }

        try{
            setorInterface.deleteById(id);
            return Map.of("message", "Setor excluído com sucesso!");
        } catch (Exception e){
            throw new RuntimeException("Erro: esse setor possui transações vinculadas e não pode ser excluido");
        }
    }

    public Page<SetorDto> buscaSetores(Long id, String descricao, Pageable pageable) {
        Page<ESetor> setores = setorInterface.findByIdAndDescricaoPartialMatch(id, descricao, pageable);
        return setores.map(setor -> modelMapper.map(setor, SetorDto.class));
    }

    @Transactional
    public Map<String, Object> altera(Long id, SetorPutDto dto){

        Optional<ESetor> setorOptional = setorInterface.findById(id);
        if(setorOptional.isEmpty()){
            throw new RuntimeException("Nenhum setor encontrado");
        }

        ESetor setor = setorOptional.get();
        modelMapper.map(dto, setor);
        setorInterface.save(setor);

        Map<String, Object> response = new HashMap<>();
        response.put("setor", modelMapper.map(setor, SetorDto.class));
        return response;
    }

    public String exportAllSetoresToCsv() {
        List<ESetor> setores = setorInterface.findAll();

        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID,Descrição,Status,Criado Em,Usuário ID,Usuário Nome\n");

        for (ESetor setor : setores) {
            csvContent.append(setor.getId()).append(",");
            csvContent.append(escapeCsv(setor.getDescricao() != null ? setor.getDescricao() : "")).append(",");
            csvContent.append(setor.getStatus()).append(",");
            csvContent.append(setor.getCreatedAt()).append(",");
            csvContent.append(setor.getUsuario() != null ? setor.getUsuario().getId() : "").append(",");
            csvContent.append(setor.getUsuario() != null ? escapeCsv(setor.getUsuario().getNome()) : "");
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
