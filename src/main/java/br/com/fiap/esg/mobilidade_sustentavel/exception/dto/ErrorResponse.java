package br.com.fiap.esg.mobilidade_sustentavel.exception.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a standardized error response for the API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)  
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,  
    String error,  
    String message,  
    String path,  
    Map<String, String> validationErrors  
) {
    /**
     * Constructor for general errors without field-specific validation details.
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }
} 