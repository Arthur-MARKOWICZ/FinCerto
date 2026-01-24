package com.finanCerto.finanCertoBack.categoria;

import com.finanCerto.finanCertoBack.categoria.dto.CategoriaCadastroDto;
import com.finanCerto.finanCertoBack.exception.CategoriaComMesmoNome;
import com.finanCerto.finanCertoBack.exception.CategoriaNaoEncontrada;
import com.finanCerto.finanCertoBack.exception.ContaNaoEncontrada;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {
    private static final Logger logger = LoggerFactory.getLogger(CategoriaService.class);
    private final CategoriaRepository repository;
    private final TokenService tokenService;

    public CategoriaService(CategoriaRepository repository, TokenService tokenService) {
        this.repository = repository;
        this.tokenService = tokenService;
    }
    
    public Page<Categoria> listarTodasPaginado(int pagina, int tamanho){
        logger.info("Listando categorias paginadas - pagina: {}, tamanho: {}", pagina, tamanho);
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Page<Categoria> categorias = repository.findAll(pageable);
        logger.info("Retornando {} categorias na pagina {}", categorias.getContent().size(), pagina);
        return categorias;
    }
    
    public List<Categoria> listarPorUsuario( String token){
        Usuario usuario = tokenService.obterUsuario(token);
        long usuarioId = usuario.getId();
        logger.info("Listando categorias do usuario: {}", usuarioId);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Categoria> categoriasPage = repository.findByUsuarioId(usuarioId, pageable);
        List<Categoria> categorias = categoriasPage.getContent();
        logger.info("Encontradas {} categorias para o usuario {}", categorias.size(), usuarioId);
        return categorias;
    }
    
    public Categoria cadastro(CategoriaCadastroDto dto){
        logger.info("tentando o cadastro da categoria: {}", dto.nome());
        Usuario usuario = tokenService.obterUsuario(dto.token());
        logger.info("Recuperado o usuario pelo token: {}", usuario);
        if (repository.existsByUsuarioIdAndNome(usuario.getId(), dto.nome())){
            throw  new CategoriaComMesmoNome("ja tem uma categoria com este nome");
        }
        Categoria categoria = new Categoria(dto);
        categoria.setUsuario(usuario);

        repository.save(categoria);
        logger.info("Categoria salva no banco de dados");
        return categoria;
    }
    public Categoria obterCategoriaPorNome(String nome){
        logger.info("tentando obter a categoria de nome: {}", nome);
        Categoria categoria = repository.findByNome(nome)
                .orElseThrow(() -> new CategoriaNaoEncontrada("A categoria não foi encontrada"));

        logger.info("categoria: {} obtida com sucesso", nome);
        return categoria;
    }
}

