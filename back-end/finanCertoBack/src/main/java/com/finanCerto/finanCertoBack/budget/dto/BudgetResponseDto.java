package com.finanCerto.finanCertoBack.budget.dto;

import java.time.LocalDate;

public record BudgetResponseDto(Long id, double limitValue, double currentValue, String name, LocalDate deadline, String categoryName) {
}
