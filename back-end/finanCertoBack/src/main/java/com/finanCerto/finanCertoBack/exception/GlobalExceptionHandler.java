package com.finanCerto.finanCertoBack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(UsuarioJaExiste.class)
        public ResponseEntity<Object> handleUsuarioJaExiste(UsuarioJaExiste ex){
            return ConstrutorResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    @ExceptionHandler(CategoriaComMesmoNome.class)
    public ResponseEntity<Object> handleCategoriaComMesmoNome(CategoriaComMesmoNome ex){
        return ConstrutorResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(ContaComOMesmoNome.class)
    public ResponseEntity<Object> handleContaComOMesmoNome(ContaComOMesmoNome ex){
        return ConstrutorResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(ContaNaoEncontrada.class)
    public ResponseEntity<Object> handleContaNaoEncontrada(ContaNaoEncontrada ex){
        return ConstrutorResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(UsuarioNaoEncontrado.class)
    public ResponseEntity<Object> handleUsuarioNaoEncontrado(UsuarioNaoEncontrado ex){
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
