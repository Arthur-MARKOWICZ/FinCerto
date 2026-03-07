package com.finanCerto.finanCertoBack.account.dtos;

import com.finanCerto.finanCertoBack.account.AccountType;

public record AccountRegistrationDto(String name, AccountType type, double initialBalance, String token) {
}
