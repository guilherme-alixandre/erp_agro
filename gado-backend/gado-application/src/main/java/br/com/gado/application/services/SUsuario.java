package br.com.gado.application.services;

import br.com.gado.application.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.application.dto.usuarioDto.UsuarioDto;
import br.com.gado.application.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SUsuario {

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;

    public UsuarioDto encontraPorEmail(String email) {
        EUsuario usuario = usuarioInterface.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("usuÃ¡rio nÃ£o encontrado"));
        return modelMapper.map(usuario, UsuarioDto.class);
    }

    @Transactional
    public UsuarioDto cadastra(UsuarioCadastroDto dto) {
        EUsuario usuario = modelMapper.map(dto, EUsuario.class);
        usuario.setDataCadastro(LocalDateTime.now());

        EUsuario usuarioSalvo = usuarioInterface.save(usuario);
        return modelMapper.map(usuarioSalvo, UsuarioDto.class);
    }

    @Transactional
    public String deleta(String email) {
        boolean existe = usuarioInterface.existsByEmail(email);
        if (!existe) {
            return "UsuÃ¡rio nÃ£o encontrado";
        }

        try {
            usuarioInterface.deleteByEmail(email);
            return "UsuÃ¡rio deletado com sucesso";
        } catch (Exception e) {
            return "NÃ£o foi possÃ­vel deletar porque ele estÃ¡ vinculado com outras entidades";
        }
    }

    @Transactional
    public UsuarioDto altera(String email, UsuarioPutDto dto) {
        EUsuario usuario = usuarioInterface.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("UsuÃ¡rio nÃ£o encontrado"));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, usuario);
        EUsuario usuarioAtualizado = usuarioInterface.save(usuario);
        return modelMapper.map(usuarioAtualizado, UsuarioDto.class);
    }
}
