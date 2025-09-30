package com.finanCerto.finanCertoBack.usuario;


import com.finanCerto.finanCertoBack.exception.UsuarioNaoEncontrado;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.dtos.LoginRequestDto;
import com.finanCerto.finanCertoBack.usuario.dtos.LoginResponseDto;
import com.finanCerto.finanCertoBack.usuario.dtos.UsuarioCadastroDto;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    public UsuarioService(UsuarioRepository repository, BCryptPasswordEncoder encoder, TokenService tokenService) {
        this.repository = repository;
        this.encoder = encoder;
        this.tokenService = tokenService;
    }
    public Usuario cadastro(UsuarioCadastroDto dto){
        Usuario usuario =  new Usuario(dto);
        String hashSenha = encoder.encode(dto.senha());
        usuario.setSenha(hashSenha);
        repository.save(usuario);
        return  usuario;
    }
    public LoginResponseDto login(LoginRequestDto dto){
        Optional<Usuario> usuario = repository.findByEmail(dto.email());
        if(usuario.isEmpty()){
            throw  new UsuarioNaoEncontrado("email ou senha incorretos");
        }
        if(!encoder.matches(dto.senha(), usuario.get().getSenha())){
            throw new UsuarioNaoEncontrado("email ou senha incorretos");
        }
        String token = tokenService.generateToken(usuario.get());
        LoginResponseDto response = new LoginResponseDto(token);
        return  response;
    }
}
