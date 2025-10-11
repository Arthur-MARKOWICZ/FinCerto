package com.finanCerto.finanCertoBack.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static  final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
        @ExceptionHandler(UsuarioJaExiste.class)
        public ResponseEntity<Object> handleUsuarioJaExiste(UsuarioJaExiste ex){
            logger.error("ja existe uma usuario com este email: {}",ex.getMessage());
            return ConstrutorResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    @ExceptionHandler(CategoriaComMesmoNome.class)
    public ResponseEntity<Object> handleCategoriaComMesmoNome(CategoriaComMesmoNome ex){
        logger.error("ja existe uma categoria com este nome: {}",ex.getMessage());
        return ConstrutorResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(ContaComOMesmoNome.class)
    public ResponseEntity<Object> handleContaComOMesmoNome(ContaComOMesmoNome ex){
            logger.error("ja existe uma conta com este nome: {}",ex.getMessage());
        return ConstrutorResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(ContaNaoEncontrada.class)
    public ResponseEntity<Object> handleContaNaoEncontrada(ContaNaoEncontrada ex){
            logger.error("conta nao foi encontrada: {}",ex.getMessage());
        return ConstrutorResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(UsuarioNaoEncontrado.class)
    public ResponseEntity<Object> handleUsuarioNaoEncontrado(UsuarioNaoEncontrado ex){
        logger.error("usuario nao foi encontrado: {}", ex.getMessage());
        return ConstrutorResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(CategoriaNaoEncontrada.class)
    public ResponseEntity<Object> handleCategoriaNaoEncontrada(CategoriaNaoEncontrada ex){
        logger.error("categoria nao foi encontrada: {}",ex.getMessage());
        return ConstrutorResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(TransacaoNaoEncontrada.class)
    public ResponseEntity<Object> handleTransacaoNaoEncontrada(TransacaoNaoEncontrada ex){
        logger.error("transacao nao foi encontrada: {}",ex.getMessage());
        return ConstrutorResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(OrcamentoComMesmoNome.class)
    public ResponseEntity<Object> handleOrcamentoComMesmoNome(OrcamentoComMesmoNome ex){
        logger.error("Ja existe um orcamento com este nome: {}",ex.getMessage());
        return ConstrutorResposta(HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(OrcamentoNaoEncontrado.class)
    public ResponseEntity<Object> handleOrcamentoNaoEncontrado(OrcamentoNaoEncontrado ex){
        logger.error("Orcamento nao foi encontrado: {}",ex.getMessage());
        return ConstrutorResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }


    private ResponseEntity<Object> ConstrutorResposta(HttpStatus status, String messagem){
        Map<String,Object> body= new HashMap<>();
        body.put("timeStamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("messagem", messagem);
        return new ResponseEntity<>(body,status);
    }
}
