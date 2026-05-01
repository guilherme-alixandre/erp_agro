package br.com.gado.controllers;

import br.com.gado.application.services.SUsuario;
import br.com.gado.application.dto.usuarioDto.UsuarioCadastroDto;
import br.com.gado.application.dto.usuarioDto.UsuarioLoginDto;
import br.com.gado.application.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.application.dto.usuarioDto.UsuarioRespostaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class CUsuario {

    @Autowired
    private SUsuario usuarioService;

    @GetMapping
    public List<UsuarioRespostaDto> listarUsuarios(
            @RequestHeader(name = "X-Admin-Email", required = false) String adminEmail){
        usuarioService.validaAdmin(adminEmail);
        return usuarioService.listarTodos();
    }

    @GetMapping("/{email}")
    public Map<String, Object> getUsuario(@PathVariable String email){
        return usuarioService.encontraPorEmail(email);
    }

    @PostMapping
    public String postUsuario(
            @RequestHeader(name = "X-Admin-Email", required = false) String adminEmail,
            @RequestBody UsuarioCadastroDto dto){
        usuarioService.validaAdmin(adminEmail);
        return usuarioService.cadastra(dto);
    }

    @PostMapping("/login")
    public Map<String, Object> postLogin(@RequestBody UsuarioLoginDto dto){
        return usuarioService.login(dto);
    }

    @DeleteMapping("/{email}")
    public String deleteUsuario(
            @RequestHeader(name = "X-Admin-Email", required = false) String adminEmail,
            @PathVariable String email){
        usuarioService.validaAdmin(adminEmail);
        return usuarioService.deleta(email);
    }

    @PutMapping("/{email}")
    public String putUsuario(@PathVariable String email,
                             @RequestBody UsuarioPutDto dto)
    {
        return usuarioService.altera(email, dto);
    }

}
