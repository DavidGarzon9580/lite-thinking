package com.litethinking.platform.orders.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record OrdenItemRequest(
        @NotNull(message = "El producto es obligatorio")
        UUID productoId,
        @Positive(message = "La cantidad debe ser mayor a cero")
        int cantidad,
        @Positive(message = "El precio debe ser positivo")
        BigDecimal precioUnitario
) {
}
