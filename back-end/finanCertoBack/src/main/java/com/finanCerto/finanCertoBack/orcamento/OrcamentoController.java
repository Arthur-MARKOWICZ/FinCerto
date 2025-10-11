package com.finanCerto.finanCertoBack.orcamento;

import com.finanCerto.finanCertoBack.orcamento.dto.OrcamentoCadastroDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    @GetMapping("/nome/{nome}")
    public ResponseEntity<Orcamento> obterPorNome( @PathVariable String nome,
                                                   @RequestHeader("Authorization") String token){
        Orcamento orcamento = service.obterPorNome(nome, token);
        return ResponseEntity.ok(orcamento);
    }
}
