package br.com.gado.controllers;

import br.com.gado.application.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.application.dto.usuarioDto.UsuarioDto;
import br.com.gado.application.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.application.services.SUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/usuarios")
public class CUsuario {

    @Autowired
    private SUsuario usuarioService;

    @GetMapping("/{email}")
    public UsuarioDto getUsuario(@PathVariable String email) {
        return usuarioService.encontraPorEmail(email);
    }

    @PostMapping
    public UsuarioDto postUsuario(@RequestBody UsuarioCadastroDto dto) {
        return usuarioService.cadastra(dto);
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
