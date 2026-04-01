package br.com.fiap.esg.mobilidade_sustentavel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ViagemRequestDto(
     

    @NotBlank(message = "Transporte não pode estar em branco")
    @Size(max = 50, message = "Transporte não pode exceder 50 caracteres")
    String transporte,

    @NotNull(message = "Distância não pode ser nula")
    @Positive(message = "Distância deve ser um valor positivo")
    BigDecimal distanciaKm,

    @NotNull(message = "Data e hora não podem ser nulas")
    LocalDateTime dataHora
     
) {} 