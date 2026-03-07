package com.finanCerto.finanCertoBack.transaction.dto;

import com.finanCerto.finanCertoBack.transaction.TransactionType;
import java.time.LocalDateTime;

public record TransactionResponseDto(Long id, double amount, LocalDateTime date, String description, TransactionType type, String accountName, String categoryName) {
}
