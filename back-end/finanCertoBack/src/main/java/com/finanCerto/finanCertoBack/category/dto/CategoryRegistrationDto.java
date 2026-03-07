package com.finanCerto.finanCertoBack.category.dto;

import com.finanCerto.finanCertoBack.category.CategoryType;

public record CategoryRegistrationDto(String name, CategoryType type, String token) {
}
