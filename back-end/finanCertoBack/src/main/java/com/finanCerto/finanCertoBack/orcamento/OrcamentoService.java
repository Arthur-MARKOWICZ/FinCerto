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
        Usuario usuario = tokenService.obterUsuario(token.replace("Bearer ", ""));
        Orcamento orcamento = repository.findByUsuarioIdAndNome( usuario.getId(), nome);
        if(orcamento == null){
            throw new OrcamentoNaoEncontrado("O orcamento nao foi encontrado");
        }
        return orcamento;
    }
}
