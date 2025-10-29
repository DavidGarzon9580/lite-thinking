package com.litethinking.platform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "Correo inválido")
        @NotBlank(message = "El correo es obligatorio")
        String email,
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
