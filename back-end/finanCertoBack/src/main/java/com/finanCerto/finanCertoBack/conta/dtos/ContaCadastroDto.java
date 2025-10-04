package com.finanCerto.finanCertoBack.conta.dtos;

import com.finanCerto.finanCertoBack.conta.Tipos;

public record ContaCadastroDto(String nome, Tipos tipos, double saldoInicial,String token) {
}
