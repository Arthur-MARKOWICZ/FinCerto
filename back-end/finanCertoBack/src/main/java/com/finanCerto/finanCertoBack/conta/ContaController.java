package com.finanCerto.finanCertoBack.conta;

import com.finanCerto.finanCertoBack.conta.dtos.ContaCadastroDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
