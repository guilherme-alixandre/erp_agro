package br.com.gado.controllers;

import br.com.gado.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.dto.usuarioDto.UsuarioDto;
import br.com.gado.dto.usuarioDto.UsuarioLoginDto;
import br.com.gado.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.dto.usuarioDto.UsuarioResumoDto;
import br.com.gado.security.JwtService;
import br.com.gado.services.SUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/usuarios")
public class CUsuario {

    @Autowired
    private SUsuario usuarioService;

    @Autowired
    private JwtService jwtService;

    /** Nome+e-mail de todos os usuários ativos — aberto a qualquer autenticado (ex: seletor de "atribuir tarefa"). */
    @GetMapping("/resumo")
    public ResponseEntity<ArrayList<UsuarioResumoDto>> getUsuariosResumo() {
        return ResponseEntity.ok(usuarioService.buscarResumoTodosAtivos());
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/{email}")
    public ResponseEntity<UsuarioDto> getUsuario(@PathVariable String email) {
        return ResponseEntity.ok(usuarioService.encontraPorEmail(email));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public ResponseEntity<ArrayList<UsuarioDto>> getUsuarios() {
        return ResponseEntity.ok(usuarioService.buscarTodos());
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<UsuarioDto> postUsuario(@RequestBody UsuarioCadastroDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastra(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UsuarioLoginDto dto) {
        UsuarioDto usuario = usuarioService.login(dto);
        String token = jwtService.gerarToken(usuario.getEmail(), usuario.getPerfil());
        return ResponseEntity.ok(Map.of("usuario", usuario, "token", token));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{email}")
    public ResponseEntity<String> deleteUsuario(@PathVariable String email) {
        return ResponseEntity.ok(usuarioService.deleta(email));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR') or #email == authentication.name")
    @PutMapping("/{email}")
    public ResponseEntity<UsuarioDto> putUsuario(@PathVariable String email, @RequestBody UsuarioPutDto dto) {
        return ResponseEntity.ok(usuarioService.altera(email, dto));
    }
}
