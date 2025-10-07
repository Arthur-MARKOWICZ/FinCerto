package com.finanCerto.finanCertoBack.categoria;

import com.finanCerto.finanCertoBack.categoria.dto.CategoriaCadastroDto;
import com.finanCerto.finanCertoBack.exception.CategoriaComMesmoNome;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CategoriaService {
    private static final Logger logger = LoggerFactory.getLogger(CategoriaService.class);
    private final CategoriaRepository repository;
    private final TokenService tokenService;

    public CategoriaService(CategoriaRepository repository, TokenService tokenService) {
        this.repository = repository;
        this.tokenService = tokenService;
    }
    public Categoria cadastro(CategoriaCadastroDto dto){
        logger.info("tentando o cadastro da categoria: {}", dto.nome());
        Usuario usuario = tokenService.obterUsuario(dto.token());
        logger.info("Recuperado o usuario pelo token: {}", usuario);
        if (repository.existsByUsuarioIdAndNome(usuario.getId(), dto.nome())){
            logger.warn("Categoria com o mesmo nome ja criada: {}", dto.nome());
            throw  new CategoriaComMesmoNome("ja tem uma categoria com este nome");
        }
        Categoria categoria = new Categoria(dto);
        categoria.setUsuario(usuario);

        repository.save(categoria);
        logger.info("Categoria salva no banco de dados");
        return categoria;
    }
}

