package com.finanCerto.finanCertoBack.categoria;

import com.finanCerto.finanCertoBack.categoria.dto.CategoriaCadastroDto;
import com.finanCerto.finanCertoBack.exception.CategoriaComMesmoNome;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import org.springframework.stereotype.Service;

@Service
public class CategoriaService {
    private final CategoriaRepository repository;
    private final TokenService tokenService;

    public CategoriaService(CategoriaRepository repository, TokenService tokenService) {
        this.repository = repository;
        this.tokenService = tokenService;
    }
    public Categoria cadastro(CategoriaCadastroDto dto){
        Usuario usuario = tokenService.obterUsuario(dto.token());
        if (repository.existsByUsuarioIdAndNome(usuario.getId(), dto.nome())){
            throw  new CategoriaComMesmoNome("ja tem uma categoria com este nome");
        }
        Categoria categoria = new Categoria(dto);
        categoria.setUsuario(usuario);
        return categoria;
    }
}

