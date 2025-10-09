package com.finanCerto.finanCertoBack.usuario;

import com.finanCerto.finanCertoBack.usuario.dtos.LoginRequestDto;
import com.finanCerto.finanCertoBack.usuario.dtos.LoginResponseDto;
import com.finanCerto.finanCertoBack.usuario.dtos.UsuarioCadastroDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<Usuario> cadastro(@RequestBody UsuarioCadastroDto dto){
        logger.info("Recebendo a requisicao de cadastro do usuario: {}",dto.email());
        Usuario usuario = service.cadastro(dto);
        logger.info("Cadastro do usuario: {} , feito com sucesso", usuario.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto){
        logger.info("Recebendo a requisicao de login do usuario: {}",dto.email());
        LoginResponseDto response = service.login(dto);
        logger.info("Login do usuario: {} , feito com sucesso", dto.email());
        return ResponseEntity.ok(response);
    }
}
