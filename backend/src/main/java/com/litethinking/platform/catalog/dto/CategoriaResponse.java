package com.litethinking.platform.catalog.dto;

import java.util.UUID;

public record CategoriaResponse(
        UUID id,
        String nombre
) {
}
