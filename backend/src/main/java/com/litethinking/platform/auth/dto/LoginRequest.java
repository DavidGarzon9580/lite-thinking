package com.litethinking.platform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "Correo invalido")
        @NotBlank(message = "El correo es obligatorio")
        String email,
        @NotBlank(message = "La contrasena es obligatoria")
        String password
) {
}
