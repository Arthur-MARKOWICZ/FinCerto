package com.finanCerto.finanCertoBack.conta;

import com.finanCerto.finanCertoBack.conta.dtos.ContaCadastroDto;
import com.finanCerto.finanCertoBack.conta.dtos.ContaObterPorNome;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contas")
public class ContaController {
    private final ContaService service;

    public ContaController(ContaService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<Conta> cadastro (@RequestBody ContaCadastroDto dto){
        Conta conta = service.cadastro(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(conta);
    }
    @PostMapping("obterPorNome")
    public ResponseEntity<Conta> obterPorNome(@RequestBody ContaObterPorNome dto){
        Conta conta = service.obterContaPorNome(dto);
        return ResponseEntity.ok(conta);
    }
    @GetMapping("/obterPorUsuario/{token}")
    public ResponseEntity<Page<Conta>> obterTodosPorIdUsuario(@PathVariable String token,
                                                              @ParameterObject Pageable pageable){
        Page<Conta> contas = service.pegarTodosPorUsuarioId(token, pageable);
        return ResponseEntity.ok(contas);
    }
}
