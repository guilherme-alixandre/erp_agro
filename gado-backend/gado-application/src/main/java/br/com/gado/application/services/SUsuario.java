package br.com.gado.application.services;

import br.com.gado.application.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.application.dto.usuarioDto.UsuarioDto;
import br.com.gado.application.dto.usuarioDto.UsuarioLoginDto;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@Service
public class SUsuario {

    private static final Pattern SHA256_HEX = Pattern.compile("^[a-fA-F0-9]{64}$");

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public UsuarioDto encontraPorEmail(String email) {
        String emailNormalizado = String.valueOf(email).trim();
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailNormalizado, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado."));
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
        if (dto == null) {
            throw new IllegalArgumentException("Dados de cadastro ausentes.");
        }
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome.");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail.");
        }
        if (dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new IllegalArgumentException("Informe a senha.");
        }
        if (dto.getPerfil() == null) {
            throw new IllegalArgumentException("Informe o perfil.");
        }

        String emailNormalizado = dto.getEmail().trim();
        if (usuarioInterface.existsByEmailAndStatus(emailNormalizado, EnStatus.A)) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        EUsuario usuario = modelMapper.map(dto, EUsuario.class);
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(sha256Hex(dto.getSenha()));
        usuario.setDataCadastro(LocalDateTime.now());

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
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado."));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, usuario);
        EUsuario usuarioAtualizado = usuarioInterface.save(usuario);
        return modelMapper.map(usuarioAtualizado, UsuarioDto.class);
    }

    @Transactional
    public UsuarioDto login(UsuarioLoginDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Informe e-mail e senha.");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail.");
        }
        if (dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new IllegalArgumentException("Informe a senha.");
        }

        String email = dto.getEmail().trim();
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(email, EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        String storedPassword = usuario.getSenha();
        if (storedPassword == null || storedPassword.isBlank()) {
            throw new IllegalArgumentException("Credenciais inválidas.");
        }

        String candidateHash = sha256Hex(dto.getSenha());

        if (isSha256Hex(storedPassword)) {
            if (!storedPassword.equalsIgnoreCase(candidateHash)) {
                throw new IllegalArgumentException("Credenciais inválidas.");
            }
        } else {
            // compat: senha antiga em texto puro (se existir) e upgrade para hash
            if (!Objects.equals(storedPassword, dto.getSenha())) {
                throw new IllegalArgumentException("Credenciais inválidas.");
            }
            usuario.setSenha(candidateHash);
            usuarioInterface.save(usuario);
        }

        return modelMapper.map(usuario, UsuarioDto.class);
    }

    private static boolean isSha256Hex(String value) {
        return value != null && SHA256_HEX.matcher(value).matches();
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(String.valueOf(value).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao processar senha.", e);
        }
    }
}
