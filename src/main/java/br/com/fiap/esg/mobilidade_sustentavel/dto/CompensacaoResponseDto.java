package br.com.fiap.esg.mobilidade_sustentavel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompensacaoResponseDto(
    Long id,
    Long usuarioId,
    String tipo,
    BigDecimal quantidade,
    LocalDateTime dataRegistro
) {} 