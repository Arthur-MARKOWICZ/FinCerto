package com.finanCerto.finanCertoBack.categoria;

import com.finanCerto.finanCertoBack.categoria.dto.CategoriaCadastroDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final  CategoriaService service;
    private static final Logger logger = LoggerFactory.getLogger(CategoriaController.class);

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<Categoria> cadastro(@RequestBody CategoriaCadastroDto dto){
        logger.info("Recebido a requisicao de cadastro de categoria: {}", dto.nome());
        Categoria categoria = service.cadastro(dto);
        logger.info("Categoria cadastrada: id= {} , nome= {}",categoria.getId(),categoria.getNome());
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }
}
