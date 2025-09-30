package com.finanCerto.finanCertoBack.usuario;

import com.finanCerto.finanCertoBack.usuario.dtos.UsuarioCadastroDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<Usuario> cadastro(@RequestBody UsuarioCadastroDto dto){
        Usuario usuario = service.cadastro(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }
}
