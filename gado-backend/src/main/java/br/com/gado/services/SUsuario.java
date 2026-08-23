package br.com.gado.services;

import br.com.gado.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.dto.usuarioDto.UsuarioDto;
import br.com.gado.dto.usuarioDto.UsuarioLoginDto;
import br.com.gado.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.dto.usuarioDto.UsuarioResumoDto;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IUsuario;
import br.com.gado.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Transactional(readOnly = true)
    public ArrayList<UsuarioResumoDto> buscarResumoTodosAtivos() {
        return usuarioInterface.findAllByStatus(EnStatus.A).stream()
                .map(usuario -> {
                    UsuarioResumoDto resumo = new UsuarioResumoDto();
                    resumo.setId(usuario.getId());
                    resumo.setNome(usuario.getNome());
                    resumo.setEmail(usuario.getEmail());
                    return resumo;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    public UsuarioDto cadastra(UsuarioCadastroDto dto) {

        if(usuarioInterface.existsByEmailAndStatus(dto.getEmail(), EnStatus.A))
            throw new RuntimeException("usuário já existe no sistema");

        EUsuario usuario = modelMapper.map(dto, EUsuario.class);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
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

        if (isBCrypt(senhaArmazenada)) {
            if (!passwordEncoder.matches(dto.getSenha(), senhaArmazenada)) {
                throw new IllegalArgumentException("Credenciais inválidas.");
            }
        } else if (isSha256Hex(senhaArmazenada)) {
            // Compatibilidade: senha antiga em SHA-256 puro — valida e faz o upgrade para BCrypt
            if (!senhaArmazenada.equalsIgnoreCase(sha256Hex(dto.getSenha()))) {
                throw new IllegalArgumentException("Credenciais inválidas.");
            }
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
            usuarioInterface.save(usuario);
        } else {
            // Compatibilidade: senha antiga em texto puro — valida e faz o upgrade para BCrypt
            if (!Objects.equals(senhaArmazenada, dto.getSenha())) {
                throw new IllegalArgumentException("Credenciais inválidas.");
            }
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
            usuarioInterface.save(usuario);
        }

        return modelMapper.map(usuario, UsuarioDto.class);
    }

    private static boolean isBCrypt(String value) {
        return value != null && value.matches("^\\$2[aby]?\\$\\d{2}\\$.{53}$");
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

        if (dto.getPerfil() != null && !SecurityUtils.isAdministrador()) {
            throw new IllegalArgumentException("Apenas administradores podem alterar o perfil de um usuário.");
        }

        // Extrai e nulifica a senha antes do ModelMapper para evitar gravar texto puro
        String novaSenha = dto.getSenha();
        dto.setSenha(null);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, usuario);

        if (novaSenha != null && !novaSenha.isBlank()) {
            usuario.setSenha(passwordEncoder.encode(novaSenha));
        }

        EUsuario usuarioAtualizado = usuarioInterface.save(usuario);
        return modelMapper.map(usuarioAtualizado, UsuarioDto.class);
    }
}
