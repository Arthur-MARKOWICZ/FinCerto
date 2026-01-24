package com.finanCerto.finanCertoBack.transacao;

import com.finanCerto.finanCertoBack.transacao.dto.TransacaoCadastroDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/transacao")
public class TranscaoController {
    private final TransacaoService service;
    private static final Logger logger = LoggerFactory.getLogger(TranscaoController.class);
    public TranscaoController(TransacaoService service) {
        this.service = service;
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint(){
        logger.info("Test endpoint called successfully!");
        return ResponseEntity.ok("Test endpoint working!");
    }

    
    @GetMapping("/conta/{nomeConta}/paginado")
    public ResponseEntity<Page<Transacao>> obterPorContaPaginado(
            @PathVariable String nomeConta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            @RequestHeader("Authorization") String token){
        logger.info("Recebido a requisicao para obter transacoes da conta {} - pagina: {}, tamanho: {}", nomeConta, pagina, tamanho);
        Page<Transacao> transacoes = service.obterPorContaPaginado(nomeConta, token, pagina, tamanho);
        logger.info("Retornando {} transacoes na pagina {}", transacoes.getContent().size(), pagina);
        return ResponseEntity.ok(transacoes);
    }
    
    @GetMapping("/categoria/{nomeCategoria}/paginado")
    public ResponseEntity<Page<Transacao>> obterPorCategoriaPaginado(
            @PathVariable String nomeCategoria,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            @RequestHeader("Authorization") String token){
        logger.info("Recebido a requisicao para obter transacoes da categoria {} - pagina: {}, tamanho: {}", nomeCategoria, pagina, tamanho);
        Page<Transacao> transacoes = service.obterPorCategoriaPaginado(nomeCategoria, token, pagina, tamanho);
        logger.info("Retornando {} transacoes na pagina {}", transacoes.getContent().size(), pagina);
        return ResponseEntity.ok(transacoes);
    }
    
    @PostMapping
    public ResponseEntity<Transacao> cadastro(@RequestBody TransacaoCadastroDto dto){
        logger.info("Recebendo requisicao de cadastro de transacao: {}",dto.descricao());
        Transacao transacao = service.cadastro(dto);
        logger.info("transacao: {} cadastrada com sucesso", dto.descricao());
        return ResponseEntity.status(HttpStatus.CREATED).body(transacao);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Transacao> obterPorId(@PathVariable Long id){
        logger.info("Recebido a requicao para obter transacao de id : {}", id);
        Transacao transacao = service.obterPorId(id);
        logger.info("transacao de id : {} obtida com sucesso", id);
        return ResponseEntity.ok(transacao);
    }
}
