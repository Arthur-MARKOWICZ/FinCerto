package com.finanCerto.finanCertoBack.usuario;


import com.finanCerto.finanCertoBack.exception.UsuarioJaExiste;
import com.finanCerto.finanCertoBack.exception.UsuarioNaoEncontrado;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.dtos.LoginRequestDto;
import com.finanCerto.finanCertoBack.usuario.dtos.LoginResponseDto;
import com.finanCerto.finanCertoBack.usuario.dtos.UsuarioCadastroDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class UsuarioService {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    public UsuarioService(UsuarioRepository repository, BCryptPasswordEncoder encoder, TokenService tokenService) {
        this.repository = repository;
        this.encoder = encoder;
        this.tokenService = tokenService;
    }
    public Usuario cadastro(UsuarioCadastroDto dto){
        logger.info("Tentando cadastrar o usuario: {}", dto.email());
        Usuario usuario =  new Usuario(dto);
        if(repository.existsByEmail(usuario.getEmail())){

            throw new UsuarioJaExiste("Usuario com este email ja existe");
        }
        String hashSenha = encoder.encode(dto.senha());
        usuario.setSenha(hashSenha);
        repository.save(usuario);
        logger.info("usuario com email: {} salvo com sucesso",usuario.getEmail());
        return  usuario;
    }
    public LoginResponseDto login(LoginRequestDto dto){
        logger.info("Tentando fazer login do usuario: {}", dto.email());
        Optional<Usuario> usuario = repository.findByEmail(dto.email());
        if(usuario.isEmpty()){

            throw  new UsuarioNaoEncontrado("email ou senha incorretos");
        }
        if(!encoder.matches(dto.senha(), usuario.get().getSenha())){
            throw new UsuarioNaoEncontrado("email ou senha incorretos");
        }
        String token = tokenService.generateToken(usuario.get());
        LoginResponseDto response = new LoginResponseDto(token);
        logger.info("login do usuario: {} feito com sucesso", dto.email());
        return  response;
    }
}
