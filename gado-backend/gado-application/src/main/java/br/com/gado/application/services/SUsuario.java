package br.com.gado.application.services;

import br.com.gado.application.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.application.dto.usuarioDto.UsuarioDto;
import br.com.gado.application.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SUsuario {

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public UsuarioDto encontraPorEmail(String email) {
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(email, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado ou inativo."));
        return modelMapper.map(usuario, UsuarioDto.class);
    }

    @Transactional(readOnly = true)
    public ArrayList<UsuarioDto> buscarTodos() {
        ArrayList<EUsuario> usuarios = usuarioInterface.findAllByStatus(EnStatus.A);
        if (usuarios.isEmpty()) {
            log.error("Erro ao buscar usuários");
            return new ArrayList<>();
        }
        return usuarios.stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDto.class))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    public UsuarioDto cadastra(UsuarioCadastroDto dto) {
        EUsuario usuario = modelMapper.map(dto, EUsuario.class);
        usuario.setDataCadastro(LocalDateTime.now());

        for (EUsuario u : usuarioInterface.findAllByStatus(EnStatus.A)) {
            if (u.getEmail().equals(usuario.getEmail())) {
                throw new RuntimeException("Já existe um usuário ativo cadastrado com este e-mail.");
            }
        }

        EUsuario usuarioSalvo = usuarioInterface.save(usuario);
        return modelMapper.map(usuarioSalvo, UsuarioDto.class);
    }

    @Transactional
    public String deleta(String email) {
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(email, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("usuário não encontrado ou inativo"));

        usuario.setStatus(EnStatus.I);

        try {
            this.usuarioInterface.save(usuario);
            return "Usuário inativado com sucesso";
        } catch (Exception e) {
            return "Erro ao inativar usuário!";
        }
    }

    @Transactional
    public UsuarioDto altera(String email, UsuarioPutDto dto) {
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(email, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, usuario);
        EUsuario usuarioAtualizado = usuarioInterface.save(usuario);
        return modelMapper.map(usuarioAtualizado, UsuarioDto.class);
    }
}
