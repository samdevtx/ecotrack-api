package br.com.fiap.esg.mobilidade_sustentavel.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CompensacaoRequestDto(
     

    @NotBlank(message = "Tipo de compensação não pode estar em branco")
    @Size(max = 100, message = "Tipo de compensação não pode exceder 100 caracteres")
    String tipo,

    @NotNull(message = "Quantidade não pode ser nula")
    @Positive(message = "Quantidade deve ser um valor positivo")
    @DecimalMin(value = "0.01", message = "Quantidade mínima é 0.01")
    @Digits(integer=10, fraction=2, message = "Quantidade pode ter no máximo 10 dígitos inteiros e 2 fracionários")
    BigDecimal quantidade
     
) {} 