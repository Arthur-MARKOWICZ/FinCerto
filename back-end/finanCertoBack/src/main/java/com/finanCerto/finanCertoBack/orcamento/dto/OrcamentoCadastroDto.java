package com.finanCerto.finanCertoBack.orcamento.dto;

import java.time.LocalDate;

public record OrcamentoCadastroDto(double valorLimite ,double valorInical, String nome, LocalDate prazo, String categoriaNome, String token) {
}
