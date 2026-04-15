package br.com.gado.application.services;

import br.com.gado.domain.entities.EUsuario;
import br.com.gado.application.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.application.dto.usuarioDto.UsuarioLoginDto;
import br.com.gado.application.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@Service
public class SUsuario {

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;

    public Map<String, Object> encontraPorEmail(String email){
        Optional<EUsuario> usuarioOptional = usuarioInterface.findByEmail(email);
        Map<String, Object> response = new HashMap<>();

        if(usuarioOptional.isEmpty()){
            response.put("Erro", "usuário não encontrado");
        }
        else {
            response.put("Usuário", usuarioOptional.get());
        }

        return response;
    }

    public Map<String, Object> login(UsuarioLoginDto dto){
        Optional<EUsuario> usuarioOptional = usuarioInterface.findByEmail(dto.getEmail());
        Map<String, Object> response = new HashMap<>();

        if(usuarioOptional.isEmpty()){
            response.put("Erro", "usuário não encontrado");
            return response;
        }

        EUsuario usuario = usuarioOptional.get();
        if(!Objects.equals(usuario.getSenha(), dto.getSenha())){
            response.put("Erro", "credenciais inválidas");
            return response;
        }

        Map<String, Object> usuarioLogado = new HashMap<>();
        usuarioLogado.put("nome", usuario.getNome());
        usuarioLogado.put("email", usuario.getEmail());
        usuarioLogado.put("perfil", usuario.getPerfil());
        usuarioLogado.put("dataCadastro", usuario.getDataCadastro());
        response.put("Usuário", usuarioLogado);
        return response;
    }

    @Transactional
    public String cadastra(UsuarioCadastroDto dto){
        EUsuario usuario = modelMapper.map(dto, EUsuario.class);
        usuario.setDataCadastro(LocalDateTime.now());

        usuarioInterface.save(usuario);
        return "Usuário salvo com sucesso";
    }

    @Transactional
    public String deleta(String email){
        boolean existe = usuarioInterface.existsByEmail(email);
        if(!existe){
            return "Usuário não encontrado";
        }

        try{
            usuarioInterface.deleteByEmail(email);
            return "Usuário deletado com sucesso";
        } catch (Exception e) {
            return "Não foi possível deletar porque ele está vinculado com outras entidades";
        }
    }

    @Transactional
    public String altera(String email, UsuarioPutDto dto){
        EUsuario usuario = usuarioInterface.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        modelMapper.map(dto, usuario);
        usuarioInterface.save(usuario);
        return "Usuário alterado com sucesso";

    }
}
