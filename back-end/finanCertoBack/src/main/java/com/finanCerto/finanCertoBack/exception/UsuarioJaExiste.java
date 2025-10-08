package com.finanCerto.finanCertoBack.exception;

public class UsuarioJaExiste extends RuntimeException {
    public UsuarioJaExiste(String message) {
        super(message);
    }
}
