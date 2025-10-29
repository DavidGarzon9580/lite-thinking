package com.litethinking.platform.orders.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrdenResponse(
        UUID id,
        Instant fecha,
        String empresaNit,
        ClienteResponse cliente,
        List<Item> items,
        BigDecimal total
) {
    public record Item(
            UUID productoId,
            String productoNombre,
            int cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {
    }
}
