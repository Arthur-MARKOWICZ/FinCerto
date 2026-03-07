package com.finanCerto.finanCertoBack.transaction.dto;

import com.finanCerto.finanCertoBack.transaction.TransactionType;
import java.time.LocalDateTime;

public record TransactionRegistrationDto(double amount, java.time.LocalDateTime date, String description, TransactionType type,
                                   String accountName, String categoryName, String token) {
}
