package com.litethinking.platform.catalog.dto;

import java.util.UUID;

public record ProductoSummary(
        UUID id,
        String codigo,
        String nombre
) {
}
