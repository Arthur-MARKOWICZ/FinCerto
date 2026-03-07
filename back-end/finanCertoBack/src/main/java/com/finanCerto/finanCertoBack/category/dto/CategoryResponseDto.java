package com.finanCerto.finanCertoBack.category.dto;

import com.finanCerto.finanCertoBack.category.CategoryType;

public record CategoryResponseDto(Long id, String name, CategoryType type) {
}
