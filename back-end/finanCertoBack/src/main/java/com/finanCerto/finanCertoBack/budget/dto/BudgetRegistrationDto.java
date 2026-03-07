package com.finanCerto.finanCertoBack.budget.dto;

import java.time.LocalDate;

public record BudgetRegistrationDto(double limitValue, double initialValue, String name, LocalDate deadline, String categoryName, String token) {
}
