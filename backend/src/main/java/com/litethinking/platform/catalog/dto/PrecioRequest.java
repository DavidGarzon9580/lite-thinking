package com.litethinking.platform.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PrecioRequest(
        @NotBlank(message = "La moneda es obligatoria")
        @Pattern(regexp = "^[A-Z]{3}$", message = "La moneda debe tener formato ISO 4217")
        String moneda,
        @NotNull(message = "El valor es obligatorio")
        @Positive(message = "El valor debe ser positivo")
        BigDecimal valor
) {
}
