package com.litethinking.platform.orders.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @Email(message = "Correo inválido")
        @NotBlank(message = "El correo es obligatorio")
        String correo
) {
}
