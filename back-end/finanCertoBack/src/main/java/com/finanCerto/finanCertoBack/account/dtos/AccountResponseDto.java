package com.finanCerto.finanCertoBack.account.dtos;

import com.finanCerto.finanCertoBack.account.AccountType;

public record AccountResponseDto(Long id, String name, AccountType type, double initialBalance) {
}
