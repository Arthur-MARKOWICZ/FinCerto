package com.finanCerto.finanCertoBack.transacao.dto;

import com.finanCerto.finanCertoBack.transacao.Tipos;

import java.time.LocalDateTime;

public record TransacaoCadastroDto(double valor, LocalDateTime data, String descricao, Tipos tipo,
                                   String nomeConta,String nomeCategoria, String token) {
}
