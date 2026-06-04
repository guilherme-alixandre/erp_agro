package br.com.gado.controllers;

import br.com.gado.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.dto.usuarioDto.UsuarioDto;
import br.com.gado.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.services.SUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/usuarios")
public class CUsuario {

    @Autowired
    private SUsuario usuarioService;

    @GetMapping("/{email}")
    public ResponseEntity<UsuarioDto> getUsuario(@PathVariable String email) {
        return ResponseEntity.ok(usuarioService.encontraPorEmail(email));
    }

    @GetMapping
    public ResponseEntity<ArrayList<UsuarioDto>> getUsuarios() {
        return ResponseEntity.ok(usuarioService.buscarTodos());
    }

    @PostMapping
    public ResponseEntity<UsuarioDto> postUsuario(@RequestBody UsuarioCadastroDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastra(dto));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<String> deleteUsuario(@PathVariable String email) {
        return ResponseEntity.ok(usuarioService.deleta(email));
    }

    @PutMapping("/{email}")
    public ResponseEntity<UsuarioDto> putUsuario(@PathVariable String email, @RequestBody UsuarioPutDto dto) {
        return ResponseEntity.ok(usuarioService.altera(email, dto));
    }
}
