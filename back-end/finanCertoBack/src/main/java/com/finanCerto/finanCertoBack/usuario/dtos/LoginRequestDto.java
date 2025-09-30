package com.finanCerto.finanCertoBack.usuario.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(@Email String email,@NotBlank String senha) {
}
