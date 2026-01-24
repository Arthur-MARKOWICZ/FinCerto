package com.finanCerto.finanCertoBack.orcamento;

import com.finanCerto.finanCertoBack.orcamento.dto.OrcamentoCadastroDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {
    private static final Logger logger = LoggerFactory.getLogger(OrcamentoController.class);
    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<Orcamento> cadastro(@RequestBody OrcamentoCadastroDto dto){
        Orcamento orcamento = service.cadastro(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(orcamento);
    }
    
    @GetMapping("/usuario/paginado")
    public ResponseEntity<Page<Orcamento>> obterPorUsuarioPaginado(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamanho,
            @RequestHeader("Authorization") String token){
        logger.info("Recebido a requisicao para obter orcamentos paginados do usuario - pagina: {}, tamanho: {}", pagina, tamanho);
        Page<Orcamento> orcamentos = service.obterPorUsuarioPaginado(token, pagina, tamanho);
        logger.info("Retornando {} orcamentos na pagina {}", orcamentos.getContent().size(), pagina);
        return ResponseEntity.ok(orcamentos);
    }
    
    @GetMapping("/categoria/{nomeCategoria}")
    public ResponseEntity<Page<Orcamento>> obterPorCategoria(
            @PathVariable String nomeCategoria,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamanho,
            @RequestHeader("Authorization") String token){
        logger.info("Recebido a requisicao para obter orcamentos paginados da categoria: {} - pagina: {}, tamanho: {}", nomeCategoria, pagina, tamanho);
        Page<Orcamento> orcamentos = service.obterPorCategoria(nomeCategoria, token, pagina, tamanho);
        logger.info("Retornando {} orcamentos da categoria {} na pagina {}", orcamentos.getContent().size(), nomeCategoria, pagina);
        return ResponseEntity.ok(orcamentos);
    }
    
    @GetMapping("/nome/{nome}")
    public ResponseEntity<Orcamento> obterPorNome( @PathVariable String nome,
                                                   @RequestHeader("Authorization") String token){
        Orcamento orcamento = service.obterPorNome(nome, token);
        return ResponseEntity.ok(orcamento);
    }
}
