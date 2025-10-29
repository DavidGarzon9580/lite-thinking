package com.litethinking.platform.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record EmpresaUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @NotBlank(message = "La direccion es obligatoria")
        String direccion,
        @NotBlank(message = "El telefono es obligatorio")
        String telefono
) {
}
