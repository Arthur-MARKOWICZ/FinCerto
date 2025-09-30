package com.finanCerto.finanCertoBack.usuario;


import com.finanCerto.finanCertoBack.usuario.dtos.UsuarioCadastroDto;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service

public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    public UsuarioService(UsuarioRepository repository, BCryptPasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }
    public Usuario cadastro(UsuarioCadastroDto dto){
        Usuario usuario =  new Usuario(dto);
        String hashSenha = encoder.encode(dto.senha());
        usuario.setSenha(hashSenha);
        repository.save(usuario);
        return  usuario;
    }
}
