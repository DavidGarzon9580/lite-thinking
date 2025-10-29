package com.litethinking.platform.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(
        @NotBlank(message = "El NIT es obligatorio")
        @Size(max = 20, message = "El NIT no debe superar 20 caracteres")
        String nit,
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @NotBlank(message = "La direccion es obligatoria")
        String direccion,
        @NotBlank(message = "El telefono es obligatorio")
        String telefono
) {
}
