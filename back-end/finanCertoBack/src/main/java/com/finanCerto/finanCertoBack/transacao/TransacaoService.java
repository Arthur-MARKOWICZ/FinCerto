package com.finanCerto.finanCertoBack.transacao;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import com.finanCerto.finanCertoBack.categoria.CategoriaService;
import com.finanCerto.finanCertoBack.conta.Conta;
import com.finanCerto.finanCertoBack.conta.ContaService;
import com.finanCerto.finanCertoBack.exception.TransacaoNaoEncontrada;
import com.finanCerto.finanCertoBack.orcamento.OrcamentoService;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.transacao.dto.TransacaoCadastroDto;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransacaoService {
    private final TransacaoRepository repository;
    private final CategoriaService categoriaService;
    private final TokenService tokenService;
    private final ContaService contaService;
    private final OrcamentoService orcamentoService;
    private static final Logger logger = LoggerFactory.getLogger(TransacaoService.class);

    public TransacaoService(TransacaoRepository repository, CategoriaService categoriaService, TokenService tokenService, ContaService contaService, OrcamentoService orcamentoService) {
        this.repository = repository;
        this.categoriaService = categoriaService;
        this.tokenService = tokenService;
        this.contaService = contaService;
        this.orcamentoService = orcamentoService;
    }

    public Page<Transacao> obterPorContaPaginado(String nomeConta, String token, int pagina, int tamanho){
        logger.info("Buscando transacoes paginadas da conta: {} - pagina: {}, tamanho: {}", nomeConta, pagina, tamanho);
        Conta conta = contaService.obterContaPorNome(nomeConta,token);

        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("id").descending());
        Page<Transacao> transacoes = repository.findByContaId(conta.getId(), pageable);
        logger.info("Retornando {} transacoes na pagina {} para a conta {}", transacoes.getContent().size(), pagina, nomeConta);
        return transacoes;
    }
    
    public Page<Transacao> obterPorCategoriaPaginado(String nomeCategoria, String token, int pagina, int tamanho){
        logger.info("Buscando transacoes paginadas da categoria: {} - pagina: {}, tamanho: {}", nomeCategoria, pagina, tamanho);
        Usuario usuario = tokenService.obterUsuario(token.replace("Bearer ", ""));
        Categoria categoria = categoriaService.obterCategoriaPorNome(nomeCategoria);
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("id").descending());
        Page<Transacao> transacoes = repository.findByCategoriaId(categoria.getId(), pageable);
        logger.info("Retornando {} transacoes na pagina {} para a categoria {}", transacoes.getContent().size(), pagina, nomeCategoria);
        return transacoes;
    }
    
    public Transacao cadastro(TransacaoCadastroDto dto){
        logger.info("Tentando cadastrar transacao: {}",dto.descricao());
        Usuario usuario = tokenService.obterUsuario(dto.token());
        Conta conta = contaService.obterContaPorNome(dto.nomeConta(), dto.token());
        Categoria categoria = categoriaService.obterCategoriaPorNome(dto.nomeCategoria());
        Transacao transacao = new Transacao(dto,usuario,categoria,conta);
        if(transacao.getTipo() == Tipos.DESPESA){
            contaService.DiminuirSaldo(transacao.getValor(), transacao.getConta());
           
            try {
                orcamentoService.adicionar(dto.valor(),categoria);
            } catch (Exception e) {
                logger.warn("Orçamento não encontrado para categoria {}, mas transação será salva: {}", categoria.getNome(), e.getMessage());
            }
        }
        else if (transacao.getTipo() == Tipos.RECEITA){
            contaService.AdicionarSaldo(transacao.getValor(), transacao.getConta());
          
            try {
                orcamentoService.adicionar(dto.valor(),categoria);
            } catch (Exception e) {
                logger.warn("Orçamento não encontrado para categoria {}, mas transação será salva: {}", categoria.getNome(), e.getMessage());
            }
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
