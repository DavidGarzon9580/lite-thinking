package com.litethinking.platform.orders.dto;

import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nombre,
        String correo
) {
}
