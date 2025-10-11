package com.finanCerto.finanCertoBack.conta;

import com.finanCerto.finanCertoBack.conta.dtos.ContaCadastroDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(ContaController.class);
    public ContaController(ContaService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<Conta> cadastro (@RequestBody ContaCadastroDto dto){
        logger.info("Recebendo requisicao de cadastro de conta: {}",dto.nome());
        Conta conta = service.cadastro(dto);
        logger.info("conta: {} cadastrada com sucesso", dto.nome());
        return ResponseEntity.status(HttpStatus.CREATED).body(conta);
    }
    @GetMapping("/nome/{nome}")
    public ResponseEntity<Conta> obterPorNome(@PathVariable  String nome,
                                              @RequestHeader("Authorization") String token){
        logger.info("Recebendo requisicao de obter conta por nome da conta: {}",nome);
        Conta conta = service.obterContaPorNome(nome,token);
        logger.info("Conta: {} obtida com sucesso",nome);
        return ResponseEntity.ok(conta);
    }
    @GetMapping("/obterPorUsuario/{token}")
    public ResponseEntity<Page<Conta>> obterTodosPorIdUsuario(@PathVariable String token,
                                                              @ParameterObject Pageable pageable){
        logger.info("Recebendo requisicao de obter contas do usuario");
        Page<Conta> contas = service.pegarTodosPorUsuarioId(token, pageable);
        logger.info("contas obtidas com sucesso");
        return ResponseEntity.ok(contas);
    }
    @PostMapping("/alterarDadosConta")
    public ResponseEntity<Conta> alterarDadosConta(@RequestBody ContaCadastroDto dto, Long id){
        logger.info("Recebendo requisicao de alterar dados de conta: {}",dto.nome());
        Conta conta = service.atualizarConta(id,dto);
        logger.info("Alteracao de dados da conta: {} feita com sucesso",dto.nome());
        return ResponseEntity.ok(conta);
    }
    @GetMapping("/saldo/{id}")
    public ResponseEntity<Double> obterSaldo(@PathVariable Long id){
        logger.info("Recebendo requisicao de obter saldo de conta: {}",id);
        Double saldo = service.pegarSaldoConta(id);
        logger.info("saldo da conta : {} obtido com sucesso",id);
        return ResponseEntity.ok(saldo);

    }
}
