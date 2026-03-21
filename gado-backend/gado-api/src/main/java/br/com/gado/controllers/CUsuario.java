package br.com.gado.controllers;

import br.com.gado.application.services.SUsuario;
import br.com.gado.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.dto.usuarioDto.UsuarioPutDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class CUsuario {

    @Autowired
    private SUsuario usuarioService;

    @GetMapping("/{email}")
    public Map<String, Object> getUsuario(@PathVariable String email){
        return usuarioService.encontraPorEmail(email);
    }

    @PostMapping("/")
    public String postUsuario(@RequestBody UsuarioCadastroDto dto){
        return usuarioService.cadastra(dto);
    }

    @DeleteMapping("/{email}")
    public String deleteUsuario(@PathVariable String email){
        return usuarioService.deleta(email);
    }

    @PutMapping("/{email}")
    public String putUsuario(@PathVariable String email,
                             @RequestBody UsuarioPutDto dto)
    {
        return usuarioService.altera(email, dto);
    }

}
