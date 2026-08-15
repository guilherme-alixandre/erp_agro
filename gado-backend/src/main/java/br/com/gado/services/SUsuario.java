package br.com.gado.services;

import br.com.gado.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.dto.usuarioDto.UsuarioDto;
import br.com.gado.dto.usuarioDto.UsuarioLoginDto;
import br.com.gado.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        if(usuarioInterface.existsByEmailAndStatus(dto.getEmail(), EnStatus.A))
            throw new RuntimeException("usuário já existe no sistema");

        EUsuario usuario = modelMapper.map(dto, EUsuario.class);
        usuario.setSenha(sha256Hex(dto.getSenha()));
        usuario.setDataCadastro(LocalDateTime.now());

        EUsuario usuarioSalvo = usuarioInterface.save(usuario);
        return modelMapper.map(usuarioSalvo, UsuarioDto.class);
    }

    @Transactional
    public UsuarioDto login(UsuarioLoginDto dto) {
        if (dto == null || dto.getEmail() == null || dto.getEmail().isBlank()
                || dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new IllegalArgumentException("Informe e-mail e senha.");
        }

        String email = dto.getEmail().trim();
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(email, EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        String senhaArmazenada = usuario.getSenha();
        if (senhaArmazenada == null || senhaArmazenada.isBlank()) {
            throw new IllegalArgumentException("Credenciais inválidas.");
        }

        String candidatoHash = sha256Hex(dto.getSenha());

        if (isSha256Hex(senhaArmazenada)) {
            if (!senhaArmazenada.equalsIgnoreCase(candidatoHash)) {
                throw new IllegalArgumentException("Credenciais inválidas.");
            }
        } else {
            // Compatibilidade: senha antiga em texto puro — valida e faz o upgrade para hash
            if (!Objects.equals(senhaArmazenada, dto.getSenha())) {
                throw new IllegalArgumentException("Credenciais inválidas.");
            }
            usuario.setSenha(candidatoHash);
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
