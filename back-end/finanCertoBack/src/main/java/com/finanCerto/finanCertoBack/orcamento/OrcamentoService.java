package com.finanCerto.finanCertoBack.orcamento;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import com.finanCerto.finanCertoBack.categoria.CategoriaService;
import com.finanCerto.finanCertoBack.exception.OrcamentoComMesmoNome;
import com.finanCerto.finanCertoBack.exception.OrcamentoNaoEncontrado;
import com.finanCerto.finanCertoBack.orcamento.dto.OrcamentoCadastroDto;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class OrcamentoService {
    private static final Logger logger = LoggerFactory.getLogger(OrcamentoService.class);
    private final OrcamentoRepository repository;
    private final CategoriaService categoriaService;
    private final TokenService tokenService;

    public OrcamentoService(OrcamentoRepository repository, CategoriaService categoriaService, TokenService tokenService) {
        this.repository = repository;
        this.categoriaService = categoriaService;
        this.tokenService = tokenService;
    }
    public Orcamento cadastro(OrcamentoCadastroDto dto){
        logger.info("tentando cadastra o orcamento: {}",dto.nome());
        Categoria categoria = categoriaService.obterCategoriaPorNome(dto.categoriaNome());
        Usuario usuario = tokenService.obterUsuario(dto.token());
        if (repository.existsByUsuarioIdAndNome(usuario.getId(), dto.nome())){
            throw  new OrcamentoComMesmoNome("ja existe um orçamento do este nome");
        }
        Orcamento orcamento =  new Orcamento(dto,categoria,usuario);
        repository.save(orcamento);
        return orcamento;
    }
    public Orcamento obterPorNome(String nome,String token){
        logger.info("tetando recuperar o orcamento de nome: {}" ,nome);
        Usuario usuario = tokenService.obterUsuario(token.replace("Bearer ", ""));
        Orcamento orcamento = repository.findByUsuarioIdAndNome( usuario.getId(), nome);
        if(orcamento == null){
            throw new OrcamentoNaoEncontrado("O orcamento nao foi encontrado");
        }
        logger.info("orcemento recuperado com sucesso");
        return orcamento;
    }
    
    public Page<Orcamento> obterPorUsuarioPaginado(String token, int pagina, int tamanho){
        logger.info("Buscando orcamentos paginados do usuario - pagina: {}, tamanho: {}", pagina, tamanho);
        Usuario usuario = tokenService.obterUsuario(token.replace("Bearer ", ""));
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("id").descending());
        Page<Orcamento> orcamentos = repository.findByUsuarioId(usuario.getId(), pageable);
        logger.info("Retornando {} orcamentos na pagina {}", orcamentos.getContent().size(), pagina);
        return orcamentos;
    }
    
    public Page<Orcamento> obterPorCategoria(String nomeCategoria, String token, int pagina, int tamanho){
        logger.info("Buscando orcamentos paginados da categoria: {} - pagina: {}, tamanho: {}", nomeCategoria, pagina, tamanho);
        Usuario usuario = tokenService.obterUsuario(token.replace("Bearer ", ""));
        Categoria categoria = categoriaService.obterCategoriaPorNome(nomeCategoria);
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("id").descending());
        Page<Orcamento> orcamentos = repository.findByCategoriaId(categoria.getId(), pageable);
        logger.info("Retornando {} orcamentos da categoria {} na pagina {}", orcamentos.getContent().size(), nomeCategoria, pagina);
        return orcamentos;
    }
    public void adicionar(double valor,Categoria categoria){
        logger.info("tetando adicionar ao orcamento de categoria: {}" ,categoria.getNome());
        Pageable pageable = PageRequest.of(0, 1); // Busca apenas o primeiro orçamento
        Page<Orcamento> orcamentosPage = repository.findByCategoriaId(categoria.getId(), pageable);
        if (!orcamentosPage.isEmpty()) {
            Orcamento orcamento = orcamentosPage.getContent().get(0); // Pega o primeiro orçamento da categoria
            var valorInical = orcamento.getValorAtual();
            var novoValor = valorInical+ valor;
            orcamento.setValorAtual(novoValor);
            logger.info("valor adicionado com sucesso ao orcamento: {}", orcamento.getNome());
        } else {
            logger.warn("Nenhum orcamento encontrado para categoria: {}", categoria.getNome());
        }
    }
}
