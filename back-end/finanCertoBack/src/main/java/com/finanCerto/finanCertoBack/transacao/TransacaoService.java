package com.finanCerto.finanCertoBack.transacao;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import com.finanCerto.finanCertoBack.categoria.CategoriaService;
import com.finanCerto.finanCertoBack.conta.Conta;
import com.finanCerto.finanCertoBack.conta.ContaService;
import com.finanCerto.finanCertoBack.conta.dtos.ContaObterPorNome;
import com.finanCerto.finanCertoBack.exception.TransacaoNaoEncontrada;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.transacao.dto.TransacaoCadastroDto;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TransacaoService {
    private final TransacaoRepository repository;
    private final CategoriaService categoriaService;
    private final TokenService tokenService;
    private final ContaService contaService;
    private static final Logger logger = LoggerFactory.getLogger(TransacaoService.class);

    public TransacaoService(TransacaoRepository repository, CategoriaService categoriaService, TokenService tokenService, ContaService contaService) {
        this.repository = repository;
        this.categoriaService = categoriaService;
        this.tokenService = tokenService;
        this.contaService = contaService;
    }
    public Transacao cadastro(TransacaoCadastroDto dto){
        logger.info("Tentando cadastrar transacao: {}",dto.descricao());
        Usuario usuario = tokenService.obterUsuario(dto.token());
        ContaObterPorNome contaObterPorNome = new ContaObterPorNome(dto.token(), dto.nomeConta());
        Conta conta = contaService.obterContaPorNome(contaObterPorNome);
        Categoria categoria = categoriaService.obterCategoriaPorNome(dto.nomeCategoria());
        Transacao transacao = new Transacao(dto,usuario,categoria,conta);
        if(transacao.getTipo() == Tipos.DESPESA){
            contaService.DiminuirSaldo(transacao.getValor(), transacao.getConta());
        }
        else if (transacao.getTipo() == Tipos.RECEITA){
            contaService.AdicionarSaldo(transacao.getValor(), transacao.getConta());
        }
        repository.save(transacao);
        logger.info("salva a transacao de descricao: {}",dto.descricao());
        return transacao;
    }
    public Transacao obterPorId (Long id){
        logger.info("Tentando obter transacao de id : {}", id);
        Optional<Transacao> transacaoOptional  = repository.findById(id);
        if(transacaoOptional.isEmpty()){
            throw  new TransacaoNaoEncontrada("A transacao nao foi encontrada");
        }
        logger.info("transacao de id : {} obtida com sucesso", id);
        return transacaoOptional.get();
    }
}
