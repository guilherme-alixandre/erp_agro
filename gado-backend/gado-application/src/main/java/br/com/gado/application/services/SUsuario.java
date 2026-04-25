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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class SUsuario {
    private static final String MSG_CREDENCIAIS_INVALIDAS = "Credenciais inválidas";

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
            response.put("Usuário", toUsuarioResponse(usuarioOptional.get()));
        }

        return response;
    }

    public Map<String, Object> login(UsuarioLoginDto dto){
        Optional<EUsuario> usuarioOptional = usuarioInterface.findByEmail(dto.getEmail());
        Map<String, Object> response = new HashMap<>();

        if(usuarioOptional.isEmpty()){
            response.put("Erro", MSG_CREDENCIAIS_INVALIDAS);
            return response;
        }

        EUsuario usuario = usuarioOptional.get();
        if(!hashPassword(dto.getSenha()).equals(usuario.getSenha())){
            response.put("Erro", MSG_CREDENCIAIS_INVALIDAS);
            return response;
        }

        response.put("Usuário", toUsuarioResponse(usuario));
        return response;
    }

    @Transactional
    public String cadastra(UsuarioCadastroDto dto){
        EUsuario usuario = modelMapper.map(dto, EUsuario.class);
        usuario.setSenha(hashPassword(dto.getSenha()));
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

    private Map<String, Object> toUsuarioResponse(EUsuario usuario){
        Map<String, Object> usuarioResponse = new HashMap<>();
        usuarioResponse.put("nome", usuario.getNome());
        usuarioResponse.put("email", usuario.getEmail());
        usuarioResponse.put("perfil", usuario.getPerfil());
        usuarioResponse.put("dataCadastro", usuario.getDataCadastro());
        return usuarioResponse;
    }

    private String hashPassword(String senha){
        if(senha == null){
            return "";
        }
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash){
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1){
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao processar senha", e);
        }
    }
}
