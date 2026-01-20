package com.finanCerto.finanCertoBack.conta;

import com.finanCerto.finanCertoBack.conta.dtos.ContaCadastroDto;
import com.finanCerto.finanCertoBack.exception.ContaComOMesmoNome;
import com.finanCerto.finanCertoBack.exception.ContaNaoEncontrada;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import com.finanCerto.finanCertoBack.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ContaService {
    private final ContaRepository repository;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private static final Logger logger = LoggerFactory.getLogger(ContaService.class);
    public ContaService(ContaRepository repository, TokenService service, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.tokenService = service;
        this.usuarioRepository = usuarioRepository;
    }
    public Conta cadastro(ContaCadastroDto dto){
        logger.info("tentando cadastrar conta: {}",dto.nome());
       Usuario usuario = tokenService.obterUsuario(dto.token());
        if(repository.existsByUsuarioIdAndNome(usuario.getId(), dto.nome())){
            logger.info("Conta ja existente com este nome: {}",dto.nome());
            throw  new ContaComOMesmoNome("ja existe uma conta com este nome");
        }
        Conta conta = new Conta(dto,usuario);
        repository.save(conta);
        logger.info("Conta: {} cadastrada com sucesso", dto.nome());
        return  conta;
    }
    public Conta obterContaPorNome( String nome,String token){
        logger.info("tentando obter conta com o nome: {}",nome);
        Usuario usuario = tokenService.obterUsuario(token.replace("Bearer ", ""));
        Optional<Conta> contaOptional = repository.findByNameAndUsuarioID(usuario.getId(), nome);
        if(contaOptional.isEmpty()){
            throw new ContaNaoEncontrada("Conta nao foi encontrada");
        }
        logger.info("Conta do nome : {} obtida com sucesso", nome);
        return contaOptional.get();
    }
    public Page<Conta> pegarTodosPorUsuarioId(String token, Pageable pageable){
        Usuario usuario = tokenService.obterUsuario(token);
        logger.info("tentando obter todas contas do usuario: {}",usuario.getNome());
        Page<Conta> contas = repository.findAllByUsuarioId(usuario.getId(), pageable);
        logger.info("todas as contas obtidas com sucesso");
        return contas;
    }
    @Transactional
    public Conta atualizarConta(Long id ,ContaCadastroDto dto){
        logger.info("tentando alterar dados da conta: {} ", dto.nome());
        Optional<Conta> contaOptional = repository.findById(id);
        if(contaOptional.isEmpty()){
            throw new ContaNaoEncontrada("A conta nao foi encontrada");
        }
        Conta conta = contaOptional.get();
        conta.setNome(dto.nome());
        conta.setSaldoInicial(dto.saldoInicial());
        conta.setTipo(dto.tipos());
        logger.info("Conta: {} alterada com sucesso", dto.nome());
        return conta;
    }

    public double pegarSaldoConta(Long id){
        logger.info("tentando pegar o saldo da conta de id: {} ",id);
        Optional<Conta> contaOptional = repository.findById(id);
        if(contaOptional.isEmpty()){
            throw new ContaNaoEncontrada("A conta nao foi encontrada");
        }
        logger.info("saldo da conta : {} obtido com sucesso", id);
        return contaOptional.get().getSaldoInicial();
    }
    public void AdicionarSaldo(double valor, Conta conta){
        double saldo = valor + conta.getSaldoInicial();
        conta.setSaldoInicial(saldo);
    }
    public void DiminuirSaldo(double valor ,Conta conta){
        double saldo = conta.getSaldoInicial() -  valor;
        conta.setSaldoInicial(saldo);
    }


}
