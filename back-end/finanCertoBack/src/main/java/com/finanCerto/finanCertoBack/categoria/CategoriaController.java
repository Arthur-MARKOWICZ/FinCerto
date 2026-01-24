package com.finanCerto.finanCertoBack.categoria;

import com.finanCerto.finanCertoBack.categoria.dto.CategoriaCadastroDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final  CategoriaService service;
    private static final Logger logger = LoggerFactory.getLogger(CategoriaController.class);

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }
    
    @GetMapping
    public ResponseEntity<Page<Categoria>> listarCategorias(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho){
        logger.info("Recebido a requisicao para listar categorias - pagina: {}, tamanho: {}", pagina, tamanho);
        Page<Categoria> categorias = service.listarTodasPaginado(pagina, tamanho);
        logger.info("Retornando {} categorias na pagina {}", categorias.getContent().size(), pagina);
        return ResponseEntity.ok(categorias);
    }
    
    @GetMapping("/usuario")
    public ResponseEntity<List<Categoria>> listarCategoriasPorUsuario(@RequestHeader(value = "Authorization", required = false) String authorization){
        logger.info("Recebido a requisicao para listar categorias do usuario logado");
        String token = authorization.replace("Bearer ", "");
        List<Categoria> categorias = service.listarPorUsuario(token);
        logger.info("Retornando {} categorias do usuario", categorias.size());
        return ResponseEntity.ok(categorias);
    }
    
    @PostMapping
    public ResponseEntity<Categoria> cadastro(@RequestBody CategoriaCadastroDto dto){
        logger.info("Recebido a requisicao de cadastro de categoria: {}", dto.nome());
        Categoria categoria = service.cadastro(dto);
        logger.info("Categoria cadastrada: id= {} , nome= {}",categoria.getId(),categoria.getNome());
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }
}
