package com.finanCerto.finanCertoBack.transacao;

import com.finanCerto.finanCertoBack.transacao.dto.TransacaoCadastroDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transacao")
public class TranscaoController {
    private final TransacaoService service;
    private static final Logger logger = LoggerFactory.getLogger(TranscaoController.class);
    public TranscaoController(TransacaoService service) {
        this.service = service;
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
