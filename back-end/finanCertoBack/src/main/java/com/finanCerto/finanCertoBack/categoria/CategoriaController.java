package com.finanCerto.finanCertoBack.categoria;

import com.finanCerto.finanCertoBack.categoria.dto.CategoriaCadastroDto;
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

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<Categoria> cadastro(@RequestBody CategoriaCadastroDto dto){
        Categoria categoria = service.cadastro(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }
}
