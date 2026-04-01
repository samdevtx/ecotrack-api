package br.com.fiap.esg.mobilidade_sustentavel.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
    @NotBlank(message = "O email não pode estar em branco.")
    @Email(message = "Formato de email inválido.")
    String email,

    @NotBlank(message = "A senha não pode estar em branco.")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    String senha
) {} 