package com.finanCerto.finanCertoBack.conta;

import com.finanCerto.finanCertoBack.conta.dtos.ContaCadastroDto;
import com.finanCerto.finanCertoBack.exception.ContaComOMesmoNome;
import com.finanCerto.finanCertoBack.exception.UsuarioNaoEncontrado;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import com.finanCerto.finanCertoBack.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ContaService {
    private final ContaRepository repository;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    public ContaService(ContaRepository repository, TokenService service, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.tokenService = service;
        this.usuarioRepository = usuarioRepository;
    }
    public Conta cadastro(ContaCadastroDto dto){
        String email = tokenService.getSubject(dto.token());
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        if(usuario.isEmpty()){
            throw  new UsuarioNaoEncontrado("Usuario nao encontrado");
        }
        if(repository.existsByUsuarioIdAndNome(usuario.get().getId(), dto.nome())){
            throw  new ContaComOMesmoNome("ja existe uma conta com este nome");
        }
        Conta conta = new Conta(dto,usuario.get());
        repository.save(conta);
        return  conta;
    }
}
