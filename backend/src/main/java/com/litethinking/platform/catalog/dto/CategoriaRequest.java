package com.litethinking.platform.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank(message = "El nombre de la categoria es obligatorio")
        String nombre
) {
}
