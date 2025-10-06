package com.finanCerto.finanCertoBack.conta;

import com.finanCerto.finanCertoBack.conta.dtos.ContaCadastroDto;
import com.finanCerto.finanCertoBack.conta.dtos.ContaObterPorNome;
import com.finanCerto.finanCertoBack.exception.ContaComOMesmoNome;
import com.finanCerto.finanCertoBack.exception.ContaNaoEncontrada;
import com.finanCerto.finanCertoBack.exception.UsuarioNaoEncontrado;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import com.finanCerto.finanCertoBack.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
       Usuario usuario = tokenService.obterUsuario(dto.token());
        if(repository.existsByUsuarioIdAndNome(usuario.getId(), dto.nome())){
            throw  new ContaComOMesmoNome("ja existe uma conta com este nome");
        }
        Conta conta = new Conta(dto,usuario);
        repository.save(conta);
        return  conta;
    }
    public Conta obterContaPorNome(ContaObterPorNome dto){
        Usuario usuario = tokenService.obterUsuario(dto.token());
        Optional<Conta> contaOptional = repository.findByNameAndUsuarioID(usuario.getId(), dto.nome());
        if(contaOptional.isEmpty()){
            throw new ContaNaoEncontrada("Conta nao foi encontrada");
        }
        return contaOptional.get();
    }
    public Page<Conta> pegarTodosPorUsuarioId(String token, Pageable pageable){
        Usuario usuario = tokenService.obterUsuario(token);
        Page<Conta> contas = repository.findAllByUsuarioId(usuario.getId(), pageable);
        return contas;
    }
    @Transactional
    public Conta atualizarConta(Long id ,ContaCadastroDto dto){
        Optional<Conta> contaOptional = repository.findById(id);
        if(contaOptional.isEmpty()){
            throw new ContaNaoEncontrada("A conta nao foi encontrada");
        }
        Conta conta = contaOptional.get();
        conta.setNome(dto.nome());
        conta.setSaldoInicial(dto.saldoInicial());
        conta.setTipo(dto.tipos());
        return conta;
    }

    public double pegarSaldoConta(Long id){
        Optional<Conta> contaOptional = repository.findById(id);
        if(contaOptional.isEmpty()){
            throw new ContaNaoEncontrada("A conta nao foi encontrada");
        }
        return contaOptional.get().getSaldoInicial();
    }


}
