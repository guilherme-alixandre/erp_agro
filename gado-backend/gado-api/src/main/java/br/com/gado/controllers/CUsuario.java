package br.com.gado.controllers;

import br.com.gado.application.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.application.dto.usuarioDto.UsuarioDto;
import br.com.gado.application.dto.usuarioDto.UsuarioLoginDto;
import br.com.gado.application.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.application.services.SUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class CUsuario {

    @Autowired
    private SUsuario usuarioService;

    @GetMapping("/{email}")
    public UsuarioDto getUsuario(@PathVariable String email) {
        return usuarioService.encontraPorEmail(email);
    }

    @GetMapping
    public List<UsuarioDto> getUsuarios() {
        return usuarioService.listarTodos();
    }

    @PostMapping
    public UsuarioDto postUsuario(@RequestBody UsuarioCadastroDto dto) {
        return usuarioService.cadastra(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioLoginDto dto) {
        if (dto == null || dto.getEmail() == null || dto.getEmail().isBlank()
                || dto.getSenha() == null || dto.getSenha().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensagem", "Informe e-mail e senha."));
        }

        try {
            UsuarioDto usuario = usuarioService.login(dto);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensagem", e.getMessage()));
        }
    }

    @DeleteMapping("/{email}")
    public String deleteUsuario(@PathVariable String email) {
        return usuarioService.deleta(email);
    }

    @PutMapping("/{email}")
    public UsuarioDto putUsuario(@PathVariable String email, @RequestBody UsuarioPutDto dto) {
        return usuarioService.altera(email, dto);
    }
}

