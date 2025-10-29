package com.litethinking.platform.auth.dto;

import com.litethinking.platform.auth.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserRequest(
        @Email(message = "Correo inválido")
        @NotBlank(message = "El correo es obligatorio")
        String email,
        @NotBlank(message = "La contraseña es obligatoria")
        String password,
        @NotNull(message = "El rol es obligatorio")
        UserRole role
) {
}
